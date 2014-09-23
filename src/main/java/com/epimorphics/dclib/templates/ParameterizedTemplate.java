/******************************************************************
 * File:        ParameterizedTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import static com.epimorphics.dclib.templates.JSONConstants.BIND;
import static com.epimorphics.dclib.templates.JSONConstants.LET;
import static com.epimorphics.dclib.templates.JSONConstants.TEMPLATE;
import static com.epimorphics.dclib.templates.JSONConstants.TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

public class ParameterizedTemplate extends TemplateBase implements Template {
    protected List<Map<String, Pattern>> parameters = new ArrayList<>();
    protected DataContext dc;
    protected Template template;
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        if (spec.hasKey(TYPE)) {
            return isLet(spec);
        } else {
            return spec.hasKey( BIND ) && spec.hasKey( TEMPLATE );
        }
    }
    
    private static boolean isLet(JsonObject spec) {
        return spec.hasKey(TYPE) && spec.get(TYPE).getAsString().value().equals(LET);
    }

    public ParameterizedTemplate(JsonObject spec, DataContext dc) {
        super(spec);
        this.dc = dc;
        if (spec.hasKey(TEMPLATE)) {
            template = getTemplateRef( spec.get(TEMPLATE), dc );
        }
        if (spec.hasKey(BIND)) {
            parseBindings( spec.get(BIND) );
        }
        if (isLet(spec) && template == null) {
            throw new EpiException("Let template did not specify a template to call");
        }
    }
    
    protected void parseBindings(JsonValue jv) {
        if (jv.isObject()) {
            parameters.add( getBindingSet( jv.getAsObject() ) );
        } else if (jv.isArray()) {
            Iterator<JsonValue> i = jv.getAsArray().iterator();
            while (i.hasNext()) {
                parseBindings(i.next());
            }
        } else {
            throw new EpiException("Illegal value for bind, bindings must be objects or arrays of objects");
        }
    }
    
    protected Map<String, Pattern> getBindingSet(JsonObject binding) {
        Map<String, Pattern> map = new HashMap<>();
        for (Entry<String, JsonValue> ent : binding.entrySet()) {
            JsonValue jv = ent.getValue();
            if (jv.isString()) {
                Pattern p = new Pattern(jv.getAsString().value(), dc);
                map.put(ent.getKey(), p);
            } else {
                throw new EpiException("Expected binding value to be a string but found: " + jv);
            }
        }
        return map;
    }

    @Override
    public Node convertRow(ConverterProcess proc, BindingEnv row, int rowNumber) {
        super.convertRow(proc, row, rowNumber);
        if (template != null) {
            return template.convertRow(proc, bindParameters(proc, row, rowNumber), rowNumber);
        } else {
            // True for a composite template with no singleton template specified
            return null;
        }
    }  
    
    protected BindingEnv bindParameters(ConverterProcess proc, BindingEnv row, int rowNumber) {
        BindingEnv env = new BindingEnv(row);
        for (Map<String, Pattern> bindingSet : parameters) {
            for (Entry<String, Pattern> ent : bindingSet.entrySet()) {
                try {
                    proc.debugCheck(env, rowNumber, ent.getValue());
                    Object value = ent.getValue().evaluate(env, proc, rowNumber);
                    env.put(ent.getKey(), value);
                } catch (NullResult e) {
                    // fall through to allow missing bindings
                    
                    // TODO was an abort - 
                    // throw new NullResult("Failed to bind variable " + ent.getKey());
                }
            }
        }
        
        // Fix up dataset binding in-case the BIND has changed the $base
        Object baseURI = env.get(ConverterProcess.BASE_OBJECT_NAME);
        if (baseURI != null && !baseURI.toString().isEmpty()) {
            Node dataset = NodeFactory.createURI(baseURI.toString());
            env.put(ConverterProcess.DATASET_OBJECT_NAME, dataset);
        }

        return env;
    }

}
