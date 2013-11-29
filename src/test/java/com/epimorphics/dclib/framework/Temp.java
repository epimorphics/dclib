/******************************************************************
 * File:        Temp.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

/**
 * Playpen used for experiments
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Temp {

    public void testJexl() {
        JexlEngine engine = new JexlEngine();
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("a", 12);
        values.put("b", "abcde");
        values.put("c", new Test());
        
        Expression expression = engine.createExpression("'I see ' + c.hello()");
        
        Object result = expression.evaluate(new MapContext(values));
        
        System.out.println(" -> " + result);
    }
    
    public static void main(String[] args) {
        
//        new Temp().testJexl();
    }
    
    public static class Test {
        public String hello() {
            return "hello from test";
        }
    }
    
    
}
