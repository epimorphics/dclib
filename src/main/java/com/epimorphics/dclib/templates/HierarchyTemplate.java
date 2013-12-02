/******************************************************************
 * File:        HierarchyTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class HierarchyTemplate extends TemplateBase implements Template {
    protected Pattern parentLink;
    protected Pattern childLink;
    protected List<Template> levelTemplates = new ArrayList<>();
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        return spec.hasKey( JSONConstants.PARENT ) || spec.hasKey( JSONConstants.CHILD );
    }
 
    public HierarchyTemplate(JsonObject spec, DataContext dc) {
        super(spec);
        
        if (spec.hasKey(JSONConstants.PARENT)) {
            parentLink = new Pattern( getRequiredField(JSONConstants.PARENT), dc );
        }
        if (spec.hasKey(JSONConstants.CHILD)) {
            childLink = new Pattern( getRequiredField(JSONConstants.CHILD), dc );
        }
        for (String key : spec.keys()) {
            if (key.startsWith("_")) {
                int index = Integer.parseInt( key.substring(1) );
                if (index >= levelTemplates.size()) {
                    for (int i = levelTemplates.size(); i < index; i++) {
                        levelTemplates.add( new TemplateRef("Filler template", dc) );
                    }
                    levelTemplates.add( getTemplateRef(spec.get(key), dc) );
                } else {
                    levelTemplates.set(index, getTemplateRef(spec.get(key), dc));
                }
            }
        }
    }
    
    public Node convertRow(ConverterProcess config, BindingEnv row, int rowNumber) {
        super.convertRow(config, row, rowNumber);
        Node[] state = (Node[]) config.getState();
        if (state == null) {
            state = new Node[ levelTemplates.size() ];
            config.setState(state);
        }
        
        Node resource = null;
        for (int i = 0; i < state.length; i++) {
            try {
                resource = levelTemplates.get(i).convertRow(config, row, rowNumber);
                if (resource != null) {
                    state[i] = resource;
                    if (i > 0 && state[i-1] != null) {
                        Node parent = state[i-1];
                        if (parentLink != null) {
                            Node link = asURINode( parentLink.evaluate(row) );
                            config.getOutputStream().triple( new Triple(resource, link, parent) );
                        }
                        if (childLink != null) {
                            Node link = asURINode( childLink.evaluate(row) );
                            config.getOutputStream().triple( new Triple(parent, link, resource) );
                        }
                    }
                }
            } catch (Exception e) {
                // No successful match so proceed to later templates
            }
        }
        return resource;
    }
    

}
