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
import com.epimorphics.util.EpiException;

import static com.epimorphics.dclib.templates.JSONConstants.*;
import com.hp.hpl.jena.graph.Node;

public class ParameterizedTemplate extends TemplateBase implements Template {
    protected Map<String, Pattern> parameters = new HashMap<String, Pattern>();
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
            JsonObject binding = spec.get(BIND).getAsObject();
            for (Entry<String, JsonValue> ent : binding.entrySet()) {
                Pattern p = new Pattern(ent.getValue().getAsString().value(), dc);
                parameters.put(ent.getKey(), p);
            }
        }
        if (isLet(spec) && template == null) {
            throw new EpiException("Let template did not specify a template to call");
        }
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
        for (Entry<String, Pattern> ent : parameters.entrySet()) {
            try {
                proc.debugCheck(row, rowNumber, ent.getValue());
                Object value = ent.getValue().evaluate(row, proc, rowNumber);
                env.put(ent.getKey(), value);
            } catch (NullResult e) {
                // TODO should this be a fatal error instead of an abort?
                throw new NullResult("Failed to bind variable " + ent.getKey());
            }
        }
        return env;
    }

}
