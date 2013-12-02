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

import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Pattern;
import com.epimorphics.dclib.framework.Template;
import com.hp.hpl.jena.graph.Node;

public class HierarchyTemplate extends TemplateBase implements Template {
    protected Pattern parentLink;
    protected Pattern childLink;
    protected List<Template> levelTemplates = new ArrayList<>();
    protected Node[] lastCreated;
    
    /**
     * Test if a json object specifies on of these templates
     */
    public static boolean isSpec(JsonObject spec) {
        return spec.hasKey( JSONConstants.PARENT ) || spec.hasKey( JSONConstants.CHILD );
    }
 
    public HierarchyTemplate(JsonObject spec, DataContext dc) {
        super(spec);
        // TODO Auto-generated constructor stub
    }

}
