/******************************************************************
 * File:        CompositeTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  12 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.NullResult;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.values.Value;
import com.epimorphics.dclib.values.ValueFactory;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

public class CompositeTemplate extends ParameterizedTemplate implements Template {
    protected List<Template> templates;
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        if (spec.hasKey(JSONConstants.TYPE)) {
            return spec.get(JSONConstants.TYPE).getAsString().value().equals(JSONConstants.COMPOSITE);
        } else {
            return spec.hasKey( JSONConstants.ONE_OFFS ) && spec.hasKey( JSONConstants.TEMPLATES );
        }
    }

    public CompositeTemplate(JsonObject spec, DataContext dc) {
        super(spec, dc);
        
        // Extract any prefixes
        if (spec.get(JSONConstants.PREFIXES) != null) {
            try {
                JsonObject prefixes = spec.get(JSONConstants.PREFIXES).getAsObject();
                for (Entry<String, JsonValue> pentry : prefixes.entrySet()) {
                    dc.setPrefix(pentry.getKey(), pentry.getValue().getAsString().value());
                }
            } catch (Exception e) {
                throw new EpiException("Couldn't parse prefix declaration in composite template", e);
            }
        }
        
        // Extract the list of to level templates to run
        templates = getTemplates(spec.get(JSONConstants.TEMPLATES), dc);
        
        // Extract any reference templates
        for (Template t : getTemplates(spec.get(JSONConstants.REFERENCED), dc)) {
            dc.registerTemplate(t);
        }
        
    }

    @Override
    public Node convertRow(ConverterProcess proc, BindingEnv row, int rowNumber) {
        super.convertRow(proc, row, rowNumber);

        BindingEnv env = bindParameters(proc, row, rowNumber);

        Node result = null;
        for (Template template : templates) {
            if (template.isApplicableTo(env)) {
                try {
                    Node n = template.convertRow(proc, env, rowNumber);
                    if (result == null && n != null) {
                        result = n;
                    }
                } catch (NullResult e) {
                    // Silently ignore null results
                } catch (Exception e) {
                    proc.getMessageReporter().report("Warning: template " + template.getName() + " applied but failed: " + e, rowNumber);
                }
            }
        }
        processRawColumns(result, proc, env, rowNumber);
        return result;
    }
    
    /**
     * Check for any columns of form "<url>" and extract those directly.
     */
    private void processRawColumns(Node resource, ConverterProcess proc, BindingEnv row, int rowNumber) {
        StreamRDF out = proc.getOutputStream();
        for (Iterator<String> i = row.allKeys(); i.hasNext(); ) {
            String col = i.next();
            if (isURI(col)) {
                String p = proc.getDataContext().expandURI( asURI(col) );
                Node predicate = NodeFactory.createURI(p);
                Object v = row.get(col);
                Node obj = null;
                if (v instanceof Value) {
                    Value val = (Value)v;
                    Object o = val.getValue();
                    if (o instanceof String) {
                        String s = (String)o;
                        if ( isURI(s) ) {
                            obj = NodeFactory.createURI( asURI(s) );
                        }
                    }
                    if (obj == null) {
                        obj = val.asNode();
                    }
                } else if (v instanceof String) {
                    String s = (String)v;
                    if ( isURI(s) ) {
                        obj = NodeFactory.createURI( asURI(s) );
                    } else {
                        obj = ValueFactory.asValue(s, proc).asNode();
                    }
                } else if (v instanceof Node) {
                    obj = (Node) v;
                } else {
                    proc.getMessageReporter().report("Warning: could not interpret raw column value: " + v, rowNumber);
                }
                out.triple( new Triple(resource, predicate, obj) );
            }
        }
    }
    
    private boolean isURI(String s) {
        return s.startsWith("<") && s.endsWith(">");
    }
    
    private String asURI(String s) {
        return s.substring(1, s.length() - 1);
    }
    
    @Override
    public void preamble(ConverterProcess proc) {
        super.preamble(proc);
        
        DataContext dc = proc.getDataContext();
        BindingEnv env = proc.getEnv();
        
        // Instantiate any global bindings
        if (spec.hasKey(JSONConstants.BIND)) {
            env = bindParameters(proc, env, -1);
        }
        
        // Process any one-offs
        for (Template t : getTemplates(spec.get(JSONConstants.ONE_OFFS), dc)) {
            t.preamble(proc);
            t.convertRow(proc, env, 0);
        }
    }
}
