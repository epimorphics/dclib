/******************************************************************
 * File:        TemplateRef.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;

/**
 * An indirect point to a template that allows for late binding of template names
 * to actual templates. Used internally.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateRef implements Template {
    protected String name;
    protected DataContext dc;
    protected Template template;
    
    public TemplateRef(String name, DataContext dc) {
        this.name = name;
        this.dc = dc;
    }

    protected Template getTemplate() {
        if (template == null) {
            template = dc.getTemplate(name);
            if (template == null) {
                throw new EpiException("Can't find template called: " + name);
            }
        }
        return template;
    }
    
    @Override
    public boolean isApplicableTo(String[] columnNames) {
        return getTemplate().isApplicableTo(columnNames);
    }

    @Override
    public Node convertRow(ConverterProcess config, BindingEnv row,
            int rowNumber) {
        return getTemplate().convertRow(config, row, rowNumber);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return getTemplate().getDescription();
    }

    @Override
    public boolean isApplicableTo(BindingEnv row) {
        return getTemplate().isApplicableTo(row);
    }

}
