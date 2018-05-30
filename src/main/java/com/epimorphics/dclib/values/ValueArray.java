/******************************************************************
 * File:        ValueStingArray.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.util.NameUtils;

import java.util.ArrayList;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.XSD;

/**
 * Wraps an array of strings, e.g. from a split operation. This allows
 * a pattern to return multiple results.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueArray extends ValueBase<Value[]> implements Value {
    
    public ValueArray(Value[] values) {
        super(values);
    }
    
    public ValueArray(String[] values) {
        super(wrapStrings(values));
    }
    
    private static Value[] wrapStrings(String[] values) {
        Value[] wrapped = new Value[ values.length ];
        for (int i = 0; i < values.length; i++) {
            wrapped[i] = new ValueString(values[i]);
        }
        return wrapped;
    }

    @Override
    public boolean isNull() {
        return value == null || value.length == 0;
    }
    
    @Override
    public boolean isMulti() {
        return true;
    }

    @Override
    public Value[] getValues() {
        return value;
    }
    
    @Override
    public Value append(Value app) {
        if (app.isMulti()) {
            Value[] apps = app.getValues();
            int len = apps.length;
            Value[] results = new Value[value.length * len];
            for (int i = 0; i < value.length; i++) {
                for (int j = 0; j < len; j++) {
                    results[i*len + j] = value[i].append( apps[j] );
                }
            }
            return new ValueArray(results);
        } else {
            String[] results = new String[value.length];
            for (int i = 0; i < value.length; i++) {
                results[i] = value[i] + app.toString();
            }
            return new ValueArray(results);
        }
    }

    @Override
    public Value asString() {
        return this;
    }

    @Override
    public Node asNode() {
        return null;
    }

    @Override
    public String getDatatype() {
        return null;
    }
    
    public Value get(int i) {
        return value[i];
    }
    
    // Value methods applicable to any type
    
    public Object datatype(final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return new ValueNode( NodeFactory.createLiteral(value.toString(), typeFor(typeURI)) );        
            }
        });
    }
    
    protected RDFDatatype typeFor(String typeURI) {
        return TypeMapper.getInstance().getSafeTypeByName( expandTypeURI(typeURI) );
    }
    
    protected String expandTypeURI(String typeURI) {
        typeURI = ConverterProcess.getGlobalDataContext().expandURI(typeURI);
        if (typeURI.startsWith("xsd:")) {
            // Hardwired xsd: even if the prefix mapping doesn't have it
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        return typeURI;
    }

    public Object format(final String fmtstr) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return new ValueString(String.format(fmtstr, value)); 
            }
        });
        
    }

    public boolean isString() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isDate() {
        return false;
    }
    
    public Value asNumber() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                ValueNumber v = new ValueNumber(value.toString());
                if (v.isNull()) {
                    reportError("Could not convert " + value + " to a number");
                }
                return v;
            }
        });
    }    
    
    @Override
    public Value map(final String mapsource, final boolean matchRequired) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsource, matchRequired);
            }
        });
    }
    
    public Value map(final String mapsource) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsource);
            }
        });
    }
    
    public Value map(final String[] mapsources, final Object deflt) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsources, deflt);
            }
        });
    }
    
    public Value asDate(final String format, final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return ValueDate.parse(value.toString(), format, expandTypeURI(typeURI));
            }
        });        
    }
    
    public Value asDate(final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return ValueDate.parse(value.toString(), expandTypeURI(typeURI));
            }
        });
    }
    
    public Value referenceTime() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
            	if(value instanceof ValueDate ) {
                   return ((ValueDate)value).referenceTime();
            	}
                reportError("Could not generate reference time for " + value + " not a ValueDate");
                return new ValueNull();
            }
        });
    }
    
    public Value toLowerCase() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap(value.toString().toLowerCase());
            }
        });
    }
    
    public Value toUpperCase() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap(value.toString().toUpperCase());
            }
        });
    }
    
    public Value toSegment() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap( NameUtils.safeName(toString()) );
            }
        });
    }
    
    public Value toCleanSegment() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).toCleanSegment();
            }
        });
    }
    
    public Value toSegment(final String repl) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).toSegment(repl);
            }
        });
    }
    
    public Value trim() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).trim();
            }
        });
    }
    
    public Value substring(final int offset) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).substring(offset);
            }
        });
    }
    
    public Value substring(final int start, final int end) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).substring(start, end);
            }
        });
    }

    public Value replaceAll(final String regex, final String replacement) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).replaceAll(regex, replacement);
            }
        });
    }

    public Value regex(final String regex) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).regex(regex);
            }
        });
    }
    
    public Value lastSegment() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).lastSegment();
            }
        });
    }
    
    public interface MapValue {
        public Value map(Value value);
    }
    
    public ValueArray applyFunction(MapValue map) {
        Value[] result = new Value[ value.length ];
        for (int i = 0; i < value.length; i++) {
            result[i] = map.map( value[i]);
        }
        return new ValueArray(result);
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder() ;
    	if (value == null) 
    		return null;
    	boolean first = true;
    	sb.append('[');
    	
    	for(Value v : value) {
    		sb.append(first ? "" : " | ");
    		sb.append(v.toString()) ;
    		first = false;
    	}
    	sb.append(']');
    	return sb.toString();
    }
    
//    public ValueArray flatten() { 
//    	ArrayList<Value> result = new ArrayList<Value>() ;
//    	Value[] a = null;
//    	for (Value v : value) {
//    		if(v instanceof ValueArray) {
//    			ValueArray values = ((ValueArray) v).flatten();
//    			for (Value x : values.getValues()) 
//    				result.add(x);
//    		}
//    		else result.add(v);    		
//    	}
//    	a = result.toArray(a);
//    	return new ValueArray(a);
//    }
}


