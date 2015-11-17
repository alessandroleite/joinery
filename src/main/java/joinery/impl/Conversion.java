/**
 *    Joinery - Data frames for Java
 *    Copyright (c) 2014, 2015 IBM Corp.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package joinery.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import joinery.DataFrame;
import joinery.DataFrame.Function;
import joinery.converters.BooleanConverter;
import joinery.converters.DateTimeConverter;
import joinery.converters.DoubleConverter;
import joinery.converters.LongConverter;
import joinery.converters.NAConverter;
import joinery.converters.StringConverter;

public class Conversion {
	
	public static final Map<Class<?>, Class<? extends Function<?, ?>>> DEFAULT_AVAILABLE_CONVERTERS = new LinkedHashMap<>();
	
	static
	{
		DEFAULT_AVAILABLE_CONVERTERS.put(Date.class, DateTimeConverter.class);
		DEFAULT_AVAILABLE_CONVERTERS.put(Boolean.class, BooleanConverter.class);
		DEFAULT_AVAILABLE_CONVERTERS.put(LongConverter.class, LongConverter.class);
		DEFAULT_AVAILABLE_CONVERTERS.put(Number.class, DoubleConverter.class);
		DEFAULT_AVAILABLE_CONVERTERS.put(DoubleConverter.class, DoubleConverter.class);
		DEFAULT_AVAILABLE_CONVERTERS.put(String.class, StringConverter.class);
	}
	
	private final Map<Class<?>, Function<Object, Object>> converters;
	
	public Conversion() {
		this.converters = new LinkedHashMap<>();
		
		 register(Date.class, new DateTimeConverter())
		.register(Boolean.class, new BooleanConverter())		
		.register(Long.class, new LongConverter())
		.register(Number.class, new DoubleConverter());
		
	}
	
	/**
	 * Register a converter for a given type.
	 * 
	 * <p>
	 * Note: it is important to notice it replaces a previous converter registered for the requested type. 
	 * </p>
	 * 
	 * @param type type to register to the converter. It might not be <code>null</code>.
	 * @param f the function to be used. It might not be <code>null</code>.
	 * @return the same instance with the given converter registered for the given type.
	 * @exception NullPointerException if the type or converter function is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public <T> Conversion register(final Class<T> type, final Function<?, ? extends T> f) {
		
		if (type == null) {
			throw new NullPointerException("Type might not be null");
		}
		
		if (f == null) {
			throw new NullPointerException("Data conversion function might not be null");
		}
		
		converters.put(type, (Function<Object, Object>) f);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Function<?, ? extends T> unregister(final Class<T> type) {
		return (Function<?, ? extends T>) converters.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Function<?, T> get(final Class<T> type) {
		return (Function<?, T>) converters.get(type);
	}
	
	
	
	/**
	 * Returns a read-only view of the registered converters.
	 * @return a read-only view of the registered converters' functions.
	 */
	public List<Function<Object, Object>> converters() {
		return Collections.unmodifiableList(new ArrayList<>(this.converters.values()));
	}
	
	
    public <V> void convert(final DataFrame df) {
        convert(df, (String) null);
    }

    public <V> void convert(final DataFrame df, final String naString) {
    	final Map<Integer, Function<?, ?>> conversions = new HashMap<>();
        final int rows = df.length();
        final int cols = df.size();

        final NAConverter<V> naConverter = new NAConverter<>(naString);
        // find conversions
        for (int c = 0; c < cols; c++) {
			for (final Function<Object, ?> conv : converters.values()) {
                boolean all = true;
                for (int r = 0; r < rows; r++) {
                    if (conv.apply(df.get(r, c)) == null && naConverter.apply(df.<V>get(r, c)) != null) {
                        all = false;
                        break;
                    }
                }
                
                if (all) {
                    conversions.put(c, conv);
                    break;
                }
            }
        }

        // apply conversions
        convert(df, conversions, naString);
    }

    @SafeVarargs
    public final <V> void convert(final DataFrame df, final Class<? extends V> ... columnTypes) {
        final Map<Integer, Function<?, ?>> conversions = new HashMap<>();
        
        for (int i = 0; i < columnTypes.length; i++) {
            final Class<? extends V> cls = columnTypes[i];
            
            if (cls != null) {
                Function<?, ?> conv = converters.get(cls);
                
                if (conv == null && DEFAULT_AVAILABLE_CONVERTERS.containsKey(cls)) {
                	conv = newInstance(DEFAULT_AVAILABLE_CONVERTERS.get(cls));
                }
                
                if (conv != null) {
                	conversions.put(i, conv);
                }
            }
        }
        
        convert(df, conversions, null);
    }

	public <V> void convert(final DataFrame df, final Map<Integer, Function<?, ?>> conversions, String naString) {
        final int cols = df.size();
        for (int c = 0; c < cols; c++) {
            final Function<?, ?> conv = conversions.get(c);
            if (conv != null) {
            	convert(df, c, conv);
            } 
            else {
            	NAConverter<V> naConverter = new NAConverter<>(naString);
            	convert(df, c, naConverter);
            }
        }
    }
    
	public <V> void convert(final DataFrame df, final Integer col, @SuppressWarnings("rawtypes") final Function f) {
		convert(df, new Integer[] { col }, new Function[] { f });
    }
    
	@SuppressWarnings("unchecked")
	public <V> void convert(final DataFrame df, final Integer[] col, @SuppressWarnings("rawtypes") final Function[] fs) {
		final int cols = col.length;
		final int rows = df.length();

		for (int i = 0; i < cols; i++) {
			for (int r = 0; r < rows; r++) {
				df.set(r, col[i], (V) fs[i].apply(df.<V> get(r, col[i])));
			}
		}
	}

    public static <V> double[][] toModelMatrix(final DataFrame df, double fillValue) {
        return toModelMatrixDataFrame(df).fillna(fillValue).toArray(double[][].class);
    }

    public static <V> double[][] toModelMatrix(final DataFrame df, double fillValue, boolean addIntercept) {
        return toModelMatrixDataFrame(df, null, addIntercept).fillna(fillValue).toArray(double[][].class);
    }

    public static <V> double[][] toModelMatrix(final DataFrame df, double fillValue, DataFrame template) {
        return toModelMatrixDataFrame(df, template, false).fillna(fillValue).toArray(double[][].class);
    }

    public static <V> double[][] toModelMatrix(final DataFrame df, double fillValue, DataFrame template, boolean addIntercept) {
        return toModelMatrixDataFrame(df, template, addIntercept).fillna(fillValue).toArray(double[][].class);
    }

    public static <V> DataFrame toModelMatrixDataFrame(final DataFrame df) {
        return toModelMatrixDataFrame(df, null, false);
    }

    /**
     *  Encodes the DataFrame as a model matrix, converting nominal values 
     *  to dummy variables and optionally adds an intercept column
     *  
     * @param df Dataframe to be converted
     * @param template template DataFrame which has already been converted
     * @param addIntercept
     * @return a new DataFrame encoded as a model matrix
     */
    public static <V> DataFrame toModelMatrixDataFrame(final DataFrame df, DataFrame template, boolean addIntercept) {
        DataFrame newDf = new DataFrame();

        if(addIntercept) {
            // Add an intercept column
            newDf.add("DFMMAddedIntercept");
            for (int i = 0; i < df.length(); i++) {
                newDf.append(Arrays.asList(1.0));
            }
        }

        final List<Object> columns = new ArrayList<>(df.columns());

        // Now convert Nominals (String columns) to dummy variables
        // Keep all others as is
        List<Class<?>> colTypes = df.types();
        for (int i = 0; i < df.size(); i++) {
            List<V> col = df.col(i);
            if(Number.class.isAssignableFrom(colTypes.get(i))) {
                List<Number> nums = new ArrayList<>();
                for (V num : col) {
                    nums.add((Number)num);
                }
                newDf.add(columns.get(i),nums);
            } else if (Date.class.isAssignableFrom(colTypes.get(i))) {
                List<Number> dates = new ArrayList<>();
                for (V date : col) {
                    dates.add(new Double(((Date)date).getTime()));
                }
                newDf.add(columns.get(i),dates);
            } else if (Boolean.class.isAssignableFrom(colTypes.get(i))) {
                List<Number> bools = new ArrayList<>();
                for (V tVal : col) {
                    bools.add((Boolean)tVal ? 1.0 : 0.0);
                }
                newDf.add(columns.get(i),bools);
            } else if (String.class.isAssignableFrom(colTypes.get(i))) {
                Set<String> namesUsed = new HashSet<String>();
                List<Object> extra = template != null ? template.col(i) : null;
                VariableToDummyResult vr = variableToDummy(col, extra);
                List<List<Number>> variable = vr.col;
                int cnt = 0;
                for(List<Number> var : variable) {
                    String name = columns.get(i) + "$" + nameToValidName(vr.names[cnt++],namesUsed);;
                    newDf.add(name, var);
                }
            }
        }

        return newDf;
    }
    
    private static Object nameToValidName(String string, Set<String> namesUsed) {
        String result = string.replaceAll("[^\\p{Alpha}]", "");
        result = result.substring(0,Math.min(result.length(),8));
        int tryCnt = 0;
        String tmp = result;
        while(namesUsed.contains(result)) {
            result = tmp + tryCnt++;
        }
        namesUsed.add(result);
        return result;
    }



    protected static class VariableToDummyResult {
        List<List<Number>> col;
        String []names;
        public VariableToDummyResult(List<List<Number>> col, String[] names) {
            super();
            this.col = col;
            this.names = names;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected static  <V> VariableToDummyResult variableToDummy(List<V> col, List<Object> extra) {
        List<List<Number>> result = new ArrayList<List<Number>>();
        Set<V> factors = new TreeSet<>(col);
        if(extra!=null)
            factors.addAll(new TreeSet(extra));
        // Convert the variable to noFactors - 1
        Iterator<V> uniqueIter = factors.iterator();
        String [] names = new String[factors.size()];
        for (int u =0; u < factors.size()-1; u++) {
            V v = uniqueIter.next();
            names[u] = v.toString();
            List<Number> newDummy = new ArrayList<Number>();
            for (int i = 0; i < col.size(); i++) {
                if(col.get(i).equals(v)) {
                    newDummy.add(1.0);
                } else {
                    newDummy.add(0.0);
                }
            }
            result.add(newDummy);
        }
        return new VariableToDummyResult(result,names);
    }


    public static <V> DataFrame isnull(final DataFrame df) {
        return df.apply(new Function<V, Boolean>() {
                @Override
                public Boolean apply(final V value) {
                    return value == null;
                }
            });
    }

    public static <V> DataFrame notnull(final DataFrame df) {
        return df.apply(new Function<V, Boolean>() {
                @Override
                public Boolean apply(final V value) {
                    return value != null;
                }
            });
    }
    
	private <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}
}
