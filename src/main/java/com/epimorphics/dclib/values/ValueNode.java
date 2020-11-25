/******************************************************************
 * File:        ValueNode.java
 * Created by:  Dave Reynolds
 * Created on:  2 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.StreamRDF;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;

public class ValueNode extends ValueBase<Node> implements Value{
    RDFNodeWrapper wnode;

    public ValueNode(Node value) {
        super(value);
    }
    
    public ValueNode(RDFNodeWrapper w) {
        super( w.asRDFNode().asNode() );
        this.wnode = w;
    }
    
    private static ValueNode makeValue(RDFNodeWrapper w) {
        return new ValueNode(w);
    }
    
    @Override
    public String toString() {
        if (value.isLiteral()) {
            return value.getLiteralLexicalForm();
        } else if (value.isURI()) {
            return value.getURI();
        } else {
            return value.getBlankNodeId().toString();
        }
    }

    @Override
    public Node asNode() {
        return value;
    }

    @Override
    public String getDatatype() {
        if (value.isLiteral()) {
            return value.getLiteralDatatypeURI();
        } else {
            return null;
        }
    }

    public RDFNodeWrapper asRDFNodeWrapper() {
        if (wnode == null) {
            ConverterProcess proc = ConverterProcess.get();
            Model model = proc.getModel();
            model.setNsPrefixes( proc.getDataContext().getPrefixes() );
            ModelWrapper wmodel = new ModelWrapper(model);
            wnode = wmodel.getNode( model.asRDFNode((Node)value) );
        }
        return wnode;
    }
    
    @Override
    public ValueNode asRDFNode() {
        return this;   // Already an RDF Node
    }


    /** tests true of the node is a literal */
    public boolean isLiteral() {
        return asRDFNodeWrapper().isLiteral();
    }

    /** Return as a Jena Literal object */
    public Literal asLiteral() {
        return asRDFNodeWrapper().asLiteral();
    }

    /** Return true if the node is an RDF list */
    public boolean isList() {
        return asRDFNodeWrapper().isList();
    }

    /** Return the contents of the RDF list as a list of wrapped nodes */
    public List<Value> asList() {
        List<Value> result = new ArrayList<>();
        for (RDFNodeWrapper w : asRDFNodeWrapper().asList()) {
            result.add( makeValue(w) );
        }
        return result;
    }

    /** Return the lexical form for a literal, the URI of a resource, the anonID of a bNode */
    public ValueString getLexicalForm() {
        return new ValueString( asRDFNodeWrapper().getLexicalForm() );
    }

    /** Return a name for the resource, falling back on curies or localnames if no naming propery is found */
    public ValueString getName() {
        return new ValueString( asRDFNodeWrapper().getName() );
    }


    /** If this is a literal return its language, otherwise return null */
    public ValueString getLanguage() {
        return new ValueString( asRDFNodeWrapper().getLanguage() );
    }


    /** Return true if this is an RDF resource */
    public boolean isResource() {
        return asRDFNodeWrapper().isResource();
    }

    /** Return true if this is an anonymous resource */
    public boolean isAnon() {
        return value.isBlank();
    }

    /** Return as a Jena Resource object */
    public Resource asResource() {
        return asRDFNodeWrapper().asResource();
    }

    /** Return the URI */
    public ValueString getURI() {
        return new ValueString( asRDFNodeWrapper().getURI() );
    }

    /** Return the shortform curi for the URI if possible, else the full URI */
    public ValueString getShortURI() {
        return new ValueString( asRDFNodeWrapper().getShortURI() );
    }


    /** Return a single value for the property of null if there is none, property can be specified using URI strings, curies or nodes */
    public ValueNode get(Object prop) {
        return getPropertyValue( toProperty(prop) );
    }

    /** Return a single value for the property of null if there is none, property can be specified using URI strings, curies or nodes */
    public ValueNode getPropertyValue(Object prop) {
        RDFNodeWrapper w = asRDFNodeWrapper().getPropertyValue( toProperty(prop) );
        if (w != null) {
            return makeValue(w);
        } else {
            return null;
        }
    }

    /** Return true if the property has the given value */
    public boolean hasResourceValue(Object prop, Object value) {
        if (value instanceof ValueNode) {
            value = ((ValueNode) value).asRDFNodeWrapper();
        }
        return asRDFNodeWrapper().hasResourceValue(toProperty(prop), value);
    }

    /** Return the value of the given property as a list of literal values or wrapped nodes, property can be specified using URI strings, curies or nodes */
    public List<ValueNode> listPropertyValues(Object prop) {
        List<RDFNodeWrapper> l = asRDFNodeWrapper().listPropertyValues(toProperty(prop));
        List<ValueNode> result = new ArrayList<ValueNode>( l.size() );
        for (RDFNodeWrapper w : l) {
            result.add( makeValue(w) );
        }
        return result;
    }

    /**
     * Return the set of property values of this node as a ordered list of value bindings
     */
    public  List<PropertyValue> listProperties() {
        return makePV( asRDFNodeWrapper().listProperties() );
    }

    private static List<PropertyValue> makePV(List<com.epimorphics.rdfutil.PropertyValue> values) {
        List<PropertyValue> result = new ArrayList<>( values.size() );
        for (com.epimorphics.rdfutil.PropertyValue pv : values) {
            PropertyValue rpv = new PropertyValue( makeValue( pv.getProp() ) );
            for (RDFNodeWrapper w : pv.getValues()) {
                rpv.addValue( makeValue(w) );
            }
            result.add( rpv );
        }
        return result;
        
    }
    
    
    /** Return list of nodes that point to us by the given property */
    public List<ValueNode> listInLinks(Object prop) {
        List<RDFNodeWrapper> l = asRDFNodeWrapper().listInLinks(toProperty(prop));
        List<ValueNode> result = new ArrayList<ValueNode>( l.size() );
        for (RDFNodeWrapper w : l) {
            result.add(  makeValue(w) );
        }
        return result;
    }

    /** Return the set of nodes which point to us  */
    public  List<PropertyValue> listInLinks() {
        return makePV( asRDFNodeWrapper().listInLinks() );
    }

    /**
     * Return the list nodes which link to this one via a SPARQL property path
     */
    public List<ValueNode> connectedNodes(Object pathv) {
        String path = pathv.toString();
        List<RDFNodeWrapper> l = asRDFNodeWrapper().connectedNodes(path);
        List<ValueNode> result = new ArrayList<ValueNode>( l.size() );
        for (RDFNodeWrapper w : l) {
            result.add( makeValue(w) );
        }
        return result;
    }

    private Object toProperty(Object prop) {
        if (prop instanceof ValueString) {
            return prop.toString();
        } else if (prop instanceof ValueNode) {
            return ((ValueNode)prop).asRDFNodeWrapper(); 
        } else {
            return prop;
        }
    }

    public ValueNode addPropertyValue(String p, String o) {
    	return addPropertyValue(new ValueString(p),  new ValueString(o)) ;
    }
    
    public ValueNode addPropertyValue(Value p, String o) {
    	return addPropertyValue(p,  new ValueString(o)) ;
    }

    public ValueNode addPropertyValue(String p, Value o) {
    	return addPropertyValue(new ValueString(p), o) ;
    }

    public ValueNode addPropertyValue(String p, Node o) {
    	return addPropertyValue(new ValueString(p), o) ;
    }

    public ValueNode addPropertyValue(Value p, Value o) {
    	return addPropertyValue(p, o.asNode());
    }
    
    public ValueNode addPropertyValue(Value p, Node o) {
        ConverterProcess proc = ConverterProcess.get();
        DataContext      dc   = proc.getDataContext();
        StreamRDF        out = proc.getOutputStream();

        Resource prop = ResourceFactory.createResource(dc.expandURI(p.toString()) );
  
        out.triple(new Triple(this.asNode(), prop.asNode(), o));
//        model.getModel().add(this.asResource(), prop, (RDFNode) val);
    
    	return this;
    }
    
    
    public ValueNode addObjectPropertyValue(String p, String o) {
    	return addObjectPropertyValue(new ValueString(p),  new ValueString(o)) ;
    }
    
    public ValueNode addObjectPropertyValue(Value p, String o) {
    	return addObjectPropertyValue(p,  new ValueString(o)) ;
    }

    public ValueNode addObjectPropertyValue(String p, Value o) {
    	return addObjectPropertyValue(new ValueString(p), o) ;
    }



    public ValueNode addObjectPropertyValue(Value p, Value o) {
        ConverterProcess proc = ConverterProcess.get();
        DataContext      dc   = proc.getDataContext();
        StreamRDF        out = proc.getOutputStream();
        
        Node prop =  ResourceFactory.createResource(dc.expandURI(p.toString())).asNode();
        Node res  = (o instanceof ValueNode)
        		    ? o.asNode() 
                    : ResourceFactory.createResource(dc.expandURI(o.toString())).asNode();
  
        out.triple(new Triple(this.asNode(), prop, res ));
//        model.getModel().add(this.asResource(), prop, (RDFNode) val);
    
    	return this;
    }
    public static class PropertyValue implements Comparable<PropertyValue> {

        protected ValueNode prop;
        protected List<ValueNode> values;
        
        public PropertyValue(ValueNode prop) {
            this.prop = prop;
            values = new ArrayList<ValueNode>();
        }
                
        public PropertyValue(ValueNode prop, ValueNode value) {
            this.prop = prop;
            values = new ArrayList<ValueNode>(1);
            values.add(value);
        }

        public List<ValueNode> getValues() {
            return values;
        }

        public void addValue(ValueNode value) {
            this.values.add( value );
        }

        public ValueNode getProp() {
            return prop;
        }

        @Override
        public int compareTo(PropertyValue o) {
            return prop.asResource().getLocalName().compareTo( o.prop.asResource().getLocalName() );
        }
    }
}
