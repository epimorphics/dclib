/******************************************************************
 * File:        TemplateRef.java
 * Created by:  Dave Reynolds
 * Created on:  2 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.templates;

import java.util.List;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.graph.Node;

/**
 * An indirect pointref to a template that allows for late binding of template names
 * to actual templates. Used internally.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateRef implements Template {
    protected String name;
    protected DataContext dc;
//    protected Template template;
    
    public TemplateRef(String name, DataContext dc) {
        this.name = name;
        this.dc = dc;
    }

    protected synchronized Template getTemplate() {
        // Switched to late binding rather than attempting to clear cached early binding
//        if (template == null) {
            Template template = dc.getTemplate(name);
            if (template == null) {
                throw new EpiException("Can't find template called: " + name);
            }
//        }
        return template;
    }
    
    /**
     * Clear any cached template dereferences, used when dynamically loading templates into a running system.
     */
    public synchronized void clearReference() {
//        template = null;
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
    public boolean isApplicableTo(ConverterProcess config, BindingEnv row, int rowNumber) {
        return getTemplate().isApplicableTo(config, row, rowNumber);
    }

    @Override
    public void preamble(ConverterProcess config, BindingEnv env) {
        getTemplate().preamble(config, env);
    }

    @Override
    public String getSource() {
        return getTemplate().getSource();
    }

    @Override
    public List<String> required() {
        return getTemplate().required();
    }

    @Override
    public List<String> optional() {
        return getTemplate().optional();
    }
    
    @Override
    public String toString() {
        return "TemplateRef-" + getName();
    }

    @Override
    public Template deref() {
        return getTemplate().deref();
    }

}
