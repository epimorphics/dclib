/******************************************************************
 * File:        ParameterizedTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.HashMap;
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
import com.hp.hpl.jena.graph.Node;

public class ParameterizedTemplate extends TemplateBase implements Template {
    protected Map<String, Pattern> parameters = new HashMap<String, Pattern>();
    protected DataContext dc;
    protected Template template;
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        return spec.hasKey( JSONConstants.BIND ) && spec.hasKey( JSONConstants.TEMPLATE );
    }

    public ParameterizedTemplate(JsonObject spec, DataContext dc) {
        super(spec);
        this.dc = dc;
        template = getTemplateRef( spec.get(JSONConstants.TEMPLATE), dc );
        JsonObject binding = spec.get(JSONConstants.BIND).getAsObject();
        for (Entry<String, JsonValue> ent : binding.entrySet()) {
            Pattern p = new Pattern(ent.getValue().getAsString().value(), dc);
            parameters.put(ent.getKey(), p);
        }
    }

    @Override
    public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        super.convertRow(config, row, rowNumber);
        BindingEnv env = new BindingEnv(row);
        for (Entry<String, Pattern> ent : parameters.entrySet()) {
            try {
                Object value = ent.getValue().evaluate(row);
                env.put(ent.getKey(), value);
            } catch (NullResult e) {
                // TODO should this be a fatal error instead of an abort?
                throw new NullResult("Failed to bind variable " + ent.getKey());
            }
        }
        return template.convertRow(config, env, rowNumber);
    }    

}
