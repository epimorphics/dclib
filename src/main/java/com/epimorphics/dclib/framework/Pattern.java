/******************************************************************
 * File:        Pattern.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;

import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueString;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Represents a string pattern in a Template, e.g. for constructing property values.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Pattern {
    static final JexlEngine engine = new JexlEngine();
    
    protected boolean isURI;
    protected boolean isInverse;
    protected boolean isConstant;
    
    protected List<Object> components = new ArrayList<>();
    
    static {
        engine.setStrict(false);
        engine.setSilent(true);
        engine.setCache(500);
    }
    
    /**
     * Compile a pattern definition into a pattern that can be later applied
     * to some set of variable values. 
     * @param pattern The pattern definition (see https://github.com/epimorphics/dclib/wiki/Template-language)
     * @param dc DataContext used for things like prefix expansion
     */
    public Pattern(String pattern, DataContext dc) {
        if (pattern.startsWith("<") && pattern.endsWith(">")) {
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
    public Object evaluate(BindingEnv env) {
        if (isConstant) {
            return components.get(0);
        } else if (components.size() == 1) {
            return evaluateComponent(0, env);
        } else {
            // Multiple components concatenated a strings
            boolean multiValued = false;
            int len = components.size();
            StringBuilder ansString = new StringBuilder();
            Value ans = null;
            for (int i = 0; i < len; i++) {
                Object result = evaluateComponent(i, env);
                if (result instanceof Value && ((Value)result).isMulti()) {
                    if (!multiValued) {
                        multiValued = true;
                        ans = new ValueString( ansString.toString() );
                    }
                }
                if (multiValued) {
                    if (result instanceof Value) {
                        ans = ans.append( (Value)result );
                    } else {
                        ans = ans.append( new ValueString(result.toString()) );
                    }
                } else {
                    ansString.append( result.toString() );
                }
            }
            if (multiValued) {
                return ans;
            } else {
                return new ValueString(ansString.toString());
            }
        }
    }


    /**
     * Interpret the pattern in some binding environment of variables.
     * Return the result converted to an RDF Node
     */
    public Node evaluateAsNode(BindingEnv env) {
        return asNode( evaluate(env) );
    }

    /**
     * Interpret the pattern in some binding environment of variables.
     * Return the result converted to a resource Node or throw an
     * exception if this is not possible
     */
    public Node evaluateAsURINode(BindingEnv env) {
        return asNode( evaluate(env) );
    }

    /**
     * Convert a pattern result to a resource Node or
     * raise an exception if this is not possible.
     */
    public Node asURINode(Object result) {
        if (result instanceof String || result instanceof ValueString) {
            return NodeFactory.createURI( result.toString() );
        } else if (result instanceof Node) {
            Node n = (Node)result;
            if (n.isBlank() || n.isURI()) {
                return n;
            }
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
        } else if (result instanceof String) {
            return NodeFactory.createLiteral( (String)result );
        } else if (result instanceof ValueString) {
            return NodeFactory.createLiteral( ((ValueString)result).getString() );
        } else if (result instanceof Number) {
            if (result instanceof BigDecimal) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDdecimal);
            } else if (result instanceof BigInteger) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDinteger);
            } else if (result instanceof Double) {
                return NodeFactory.createUncachedLiteral(result, XSDDatatype.XSDdouble);
            } else {
                return NodeFactory.createUncachedLiteral(((Number)result).intValue(), XSDDatatype.XSDinteger);
            }
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

    protected void parse(String pattern) {
        boolean escaped = false;
        boolean isScript = false;
        int blockDepth = 0;
        int len = pattern.length();
        StringBuilder block = new StringBuilder();
        
        for (int i = 0; i < len; i++) {
            char c = pattern.charAt(i);
            
            if (escaped) {
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
                    if (pattern.charAt(i+1) == '=') {
                        isScript = true;
                        i++;
                    } else {
                        isScript = false;
                    }
                } else {
                    block.append(c);
                }
                blockDepth++;
                break;
                
            case '}' :
                --blockDepth;
                if (blockDepth == 0) {
                    // Finish an expression block
                    String src = block.toString();
                    components.add( isScript ? engine.createScript(src) : engine.createExpression(src) );
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
