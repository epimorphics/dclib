/******************************************************************
 * File:        Pattern.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.NotSupportedException;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.values.GlobalFunctions;
import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueError;
import com.epimorphics.dclib.values.ValueFunction;
import com.epimorphics.dclib.values.ValueNode;
import com.epimorphics.dclib.values.ValueNumber;
import com.epimorphics.dclib.values.ValueString;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.tasks.ProgressReporter;
import com.epimorphics.util.EpiException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabelFactory;

/**
 * Represents a string pattern in a Template, e.g. for constructing property values.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Pattern {
    static final Logger log = LoggerFactory.getLogger( Pattern.class );
    static final JexlEngine engine = new JexlEngine();
    
    protected boolean isURI;
    protected boolean isInverse;
    protected boolean isConstant;
    
    protected List<Object> components = new ArrayList<>();
    
    static {
        engine.setStrict(false);
        engine.setSilent(true);
        engine.setCache(500);
        
        engine.setFunctions( GlobalFunctions.getFunctions() );
    }
        
    /**
     * Compile a pattern definition into a pattern that can be later applied
     * to some set of variable values. 
     * @param pattern The pattern definition (see https://github.com/epimorphics/dclib/wiki/Template-language)
     * @param dc DataContext used for things like prefix expansion
     */
    public Pattern(String pattern, DataContext dc) {
        pattern = pattern.trim();
        if (pattern.startsWith("\\<") || pattern.startsWith("\\^")) {
            parse(pattern.substring(1));
        } else if (pattern.startsWith("<") && pattern.endsWith(">")) {
            isURI = true;
            parse( pattern.substring(1, pattern.length() - 1) );
        } else if (pattern.startsWith("^<") && pattern.endsWith(">")) {
            isURI = true;
            isInverse = true;
            parse( pattern.substring(2, pattern.length() - 1) );
        } else {
            parse(pattern);
        }
        expandPrefixes(dc);
    }
    
    
    public boolean isURI() {
        return isURI;
    }


    public boolean isInverse() {
        return isInverse;
    }


    /**
     * Interpret the pattern in some binding environment of variables.
     */
    public Object evaluate(BindingEnv env, ConverterProcess proc, int rowNumber) {
        if (isConstant) {
            return wrapResult( components.get(0) );
            
        } else if (components.size() == 1) {
            Object result = evaluateComponent(0, env);
            checkForError(result, proc, rowNumber);
            return wrapResult(result);

        } else {
            // Multiple components concatenated a strings
            boolean multiValued = false;
            int len = components.size();
            StringBuilder ansString = new StringBuilder();
            Value ans = null;
            for (int i = 0; i < len; i++) {
                Object result = evaluateComponent(i, env);
                checkForError(result, proc, rowNumber);
                if (result instanceof Value && ((Value)result).isMulti()) {
                    if (!multiValued) {
                        multiValued = true;
                        ans = wrapResultAsValue( ansString.toString() );
                    }
                }
                if (multiValued) {
                	ans = ans.append( wrapResultAsValue(result) );
                } else {
                    ansString.append( result.toString() );
                }
            }
            if (multiValued) {
                return ans;
            } else {
                String result = ansString.toString();
                return wrapResult(result);
            }
        }
    }
    
    private Value wrapResultAsValue(Object result) {
    	Object r = wrapResult(result);
    	if (r instanceof Value) {
    		return (Value)r;
    	}
    	r = wrapResult(result.toString());
    	if (r instanceof Value) {
    		return (Value)r;
    	}
    	throw new NotSupportedException("Cannot wrap object "+result);
    }
    
    private Object wrapResult(Object result) {
        if (isURI) {
            if (result instanceof String || result instanceof ValueString) {
                String val = ConverterProcess.getGlobalDataContext().expandURI( result.toString() );
                return new ValueNode( NodeFactory.createURI(val) );
            }
        } 
        if (result instanceof String) {
            return new ValueString( (String)result );
        }
        return result;
    }
    
    private void checkForError(Object result, ConverterProcess proc, int rowNumber) {
        if (result instanceof ValueError) {
            ValueError err = (ValueError)result;
            ProgressReporter reporter = proc.getMessageReporter();
            reporter.report(err.getErrorMessage(), rowNumber);
            if (err.isFatal()) {
                reporter.setFailed();
            }
            throw new NullResult(err.getErrorMessage());
        }        
    }


    /**
     * Interpret the pattern in some processing context.
     * Return the result converted to an RDF Node
     */
    public Node evaluateAsNode(BindingEnv env, ConverterProcess proc, int rowNumber) {
        return asNode( evaluate(env, proc, rowNumber) );
    }

    /**
     * Interpret the pattern in some binding environment of variables.
     * Return the result converted to a resource Node or throw an
     * exception if this is not possible
     */
    public Node evaluateAsURINode(BindingEnv env, ConverterProcess proc, int rowNumber) {
        return asNode( evaluate(env, proc, rowNumber) );
    }

    /**
     * Convert a pattern result to a resource Node or
     * raise an exception if this is not possible.
     */
    public Node asURINode(Object result) {
        if (result instanceof String || result instanceof ValueString) {
            return NodeFactory.createURI( result.toString() );
        } 
        Node n = null;
        if (result instanceof Value) {
            n = ((Value)result).asNode();
        } else if (result instanceof Node) {
            n = (Node) result;
        } else if (result instanceof RDFNodeWrapper) {
            n = ((RDFNodeWrapper)result).asRDFNode().asNode();
        }
        if (n == null) {
            throw new EpiException("Pattern " + this + " returned null result");
        }
        if (n.isBlank() || n.isURI()) {
            return n;
        }
        throw new EpiException("Found " + result + " when expecting a URI");
    }

    /**
     * Convert a result from this pattern to an RDF Node
     */
    public Node asNode(Object result) {
        // Assumes we have already taken care of multiple valued objects
        if (isURI()) {
            return asURINode(result);
        } else if (result instanceof Node) {
            return (Node) result;
        } else if (result instanceof RDFNodeWrapper) {
            return ((RDFNodeWrapper)result).asRDFNode().asNode();
        } else if (result instanceof String) {
            return NodeFactory.createLiteralString( (String)result );
        } else if (result instanceof Number) {
            return ValueNumber.nodeFromNumber( (Number)result );
        } else if (result instanceof Boolean) {
            return NodeFactory.createLiteralDT(result.toString(), XSDDatatype.XSDboolean);
        } else if (result instanceof Value) {
            return ((Value)result).asNode();
        } else {
            // TODO handle dates
        }                
        return null;
    }
    
    protected Object evaluateComponent(int i, BindingEnv env) {
        Object component = components.get(i);
        Object result = null;
        if (component instanceof String) {
            result = component;
        } else if (component instanceof Expression) {
            result = ((Expression)component).evaluate(env);
        } else if (component instanceof Script) {
            result = ((Script)component).execute(env);
        } else if (component instanceof ValueFunction) {
            ((ValueFunction)component).setBindingEnv(env);
            return component;
        }  else {
            // Can't happen
            throw new EpiException("Internal state error in pattern evaluation");
        }
        if (result == null) {
            throw new NullResult();
        }
        return result;
    }
    
    protected void expandPrefixes(DataContext dc) {
        if (isURI && isConstant) {
            components.set(0, dc.expandURI((String)components.get(0)));
        }
    }
    
    protected static final String ESCAPED = "<>{}=";

    protected void parse(String pattern) {
        boolean escaped = false;
        boolean isScript = false;
        boolean isFunction = false;
        int blockDepth = 0;
        int len = pattern.length();
        StringBuilder block = new StringBuilder();
        
        for (int i = 0; i < len; i++) {
            char c = pattern.charAt(i);
            
            if (escaped) {
                if (ESCAPED.indexOf(c) == -1) {
                    block.append('\\');
                }
                block.append(c);
                escaped = false;
                continue;
            }
            
            switch(c) {
            case '\\' :
                escaped = true;
                break;
            
            case '{' :
                if (blockDepth == 0) {
                    // Start new expression block
                    if (block.length() > 0) {
                        components.add( block.toString() );
                    }
                    block = new StringBuilder();
                } else {
                    block.append(c);
                }
                blockDepth++;
                if (blockDepth > 1) {
                    isScript = true;
                }
                if (pattern.charAt(i+1) == '=') {
                    i++;
                    isFunction = true;
                }
                break;
                
            case '}' :
                --blockDepth;
                if (blockDepth == 0) {
                    // Finish an expression block
                    String src = block.toString();
                    try {
                        if (isFunction) {
                            components.add( new ValueFunction( engine.createScript(src)) );
                        } else {
                            components.add( isScript ? engine.createScript(src) : engine.createExpression(src) );
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse pattern: " + src, e);
                    }
                    isScript = false;
                    isFunction = false;
                    block = new StringBuilder();
                } else {
                    block.append(c);
                }
                break;
                
            default :
                block.append(c);
                break;
                    
            }
        }
        
        if (block.length() > 0) {
            components.add( block.toString() );
        }
        
        if (components.size() == 0) {
            isConstant = true;
            components.add("");
        } if (components.size() == 1 && components.get(0) instanceof String) {
            isConstant = true;
        }
        
    }
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Object component : components) {
            if (component instanceof String) {
                str.append((String)component);
            } else {
                str.append("{");
                str.append(component.toString());
                str.append("}");
            }
        }
        String out = str.toString();
        if (isURI) {
            out = "<" + out + ">";
        }
        if (isInverse) {
            out = "^" + out;
        }
        return out;
    }

}
