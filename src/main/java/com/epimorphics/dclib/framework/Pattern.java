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

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;

import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueString;
import com.epimorphics.util.EpiException;

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
    

}
