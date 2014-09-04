/******************************************************************
 * File:        Temp.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.framework;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.jena.atlas.json.JsonValue;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.dclib.values.Row;
import com.epimorphics.json.JsonUtil;
import com.hp.hpl.jena.rdf.model.Model;

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
        values.put("$row", new Row(42));
        
        Expression expression = engine.createExpression("'I see ' + $row.bnode");
        
        Object result = expression.evaluate(new MapContext(values));
        
        System.out.println(" -> " + result);
    }
    
    public void testTemplate() throws IOException {
        ConverterService service = new ConverterService();
        service.put("$base", "http://example.com/");
        String der = "/home/der/epimorphics/projects/registries/DefraPilot/data/";
        Model m = service.simpleConvert(der + "simple-skos.json", der + "category.csv");
        if (m != null) {
            m.write(System.out, "Turtle");
        }
    }
    
    public void testYamlParse(String file) throws IOException {
        Yaml yaml = new Yaml();
        Object result = yaml.load( new FileInputStream(file));
        JsonValue val = JsonUtil.asJson(result);
        System.out.println(val.toString());
    }
    
    
    public static void main(String[] args) throws IOException {
        new Temp().testYamlParse("test/mapping/sampling-points.json");
//        new Temp().testTemplate();
//        new Temp().testJexl();
    }
    
    public static class Test {
        public String hello() {
            return "hello from test";
        }
    }
    
    
}
