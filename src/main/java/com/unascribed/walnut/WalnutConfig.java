package com.unascribed.walnut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.unascribed.walnut.value.BooleanValue;
import com.unascribed.walnut.value.DoubleValue;
import com.unascribed.walnut.value.IntValue;
import com.unascribed.walnut.value.LongValue;
import com.unascribed.walnut.value.NullValue;
import com.unascribed.walnut.value.StringValue;
import com.unascribed.walnut.value.Value;

/**
 * Represents a mapping of String keys to values of varying types.
 * <p>
 * Utility methods for loading are supplied in the form of the from* methods.
 * A WalnutConfig can also be created directly via a constructor to make an
 * empty config.
 * <p>
 * For convenience, put* and get* methods are supplied for easy conversion to
 * and from Value objects. If you need more control, such as the ability to set
 * the exact serialization string of a value, or the documentation of a key, use
 * the raw {@link #put(Key, Value)} method.
 * 
 * @since 0.0.1
 */
public class WalnutConfig implements Cloneable, Value {
	private Key keyGoat = new Key(null, null);
	protected Map<Key, Value> map = new HashMap<Key, Value>();
	
	////////// INSTANCE
	
	public void putString(String key, String value) {
		_put(key, new StringValue("\""+value
			.replace("\\", "\\\\")
			.replace("\n", "\\n")
			.replace("\"", "\\\"")+"\"", value));
	}
	public void putInt(String key, int value) { _put(key, new IntValue(Integer.toString(value), value)); }
	public void putLong(String key, long value) { _put(key, new LongValue(Long.toString(value), value)); }
	public void putDouble(String key, double value) { _put(key, new DoubleValue(Double.toString(value), value)); }
	public void putBoolean(String key, boolean value) { _put(key, new BooleanValue(Boolean.toString(value), value)); }
	public void putNull(String key) { _put(key, new NullValue("null")); }
	
	private void _put(String key, Value value) {
		map.put(new Key(key, null), value);
	}
	
	
	public void put(Key key, Value value) {
		map.put(key, value);
	}
	
	public String getString(String key) {
		Value v = get(key);
		if (v instanceof StringValue) {
			return ((StringValue)v).value;
		} else {
			return v.getRawValue();
		}
	}
	public boolean getBoolean(String key) { return _get(key, BooleanValue.class).value; }
	public double getDouble(String key) { return _get(key, DoubleValue.class).value; }
	public int getInt(String key) { return _get(key, IntValue.class).value; }
	public long getLong(String key) { return _get(key, LongValue.class).value; }
	
	public boolean containsKey(String key) { return get(key) != null; }
	/**
	 * @return {@code true} if the entry exists and is null, or {@code false} if the entry does not exist or is not null.
	 */
	public boolean isNull(String key) { return get(key) instanceof NullValue; }
	
	@SuppressWarnings("unchecked") // it IS checked, damn it
	private <T extends Value> T _get(String key, Class<T> clazz) {
		Value v = get(key);
		if (v == null) {
			throw new IllegalArgumentException(key);
		}
		if (v instanceof NullValue) {
			return null;
		}
		if (clazz.isInstance(v)) {
			return (T) v;
		} else {
			throw new ClassCastException(v.getClass().getSimpleName()+" cannot be cast to "+clazz.getSimpleName());
		}
	}
	
	public Value get(String key) {
		WalnutConfig section = this;
		String[] path = key.split("\\.");
		for (int i = 0; i < path.length-1; i++) {
			Value v = section.get(path[i]);
			if (v instanceof WalnutConfig) {
				section = (WalnutConfig) v;
			} else {
				throw new ClassCastException("attempt to traverse into non-section "+path[i]+" while resolving "+key);
			}
		}
		if (path.length == 0) return null;
		keyGoat.key = path[path.length-1];
		return section.map.get(keyGoat);
	}
	
	public Value get(Key key) {
		return map.get(key);
	}
	
	public Set<Map.Entry<Key, Value>> entrySet() {
		return map.entrySet();
	}
	
	
	@Override
	public WalnutConfig clone() {
		WalnutConfig o;
		try {
			o = (WalnutConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
		o.map = new HashMap<Key, Value>();
		for (Map.Entry<Key, Value> en : map.entrySet()) {
			o.map.put(en.getKey(), en.getValue().clone());
		}
		return o;
	}
	
	@Override
	public String getRawValue() {
		return toString();
	}
	
	@Override
	public boolean equalsIgnoreRaw(Value v) {
		return equals(v);
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WalnutConfig other = (WalnutConfig) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
	
	////////// INSTANCE STORAGE METHODS
	

	/**
	 * Serializes this WalnutConfig into a String.
	 * <p>
	 * Colons will be used for key-value separation in pairs, and
	 * tabs will be used for indentation.
	 * 
	 * @since 0.0.1
	 */
	@Override
	public String toString() {
		return toString(SerializationStyle.COLONS_TABS);
	}
	
	/**
	 * Serializes this WalnutConfig into a String.
	 * 
	 * @param style a definition of how to style the output, e.g. indentation type,
	 * 			whether to use colons or equals, etc
	 * @return this config, serialized to a properly formatted Walnut config string,
	 * 			following the passed style
	 * @since 0.0.1
	 */
	public String toString(SerializationStyle style) {
		return map.toString();
	}
	
	
	////////// STATIC CONSTRUCTION METHODS
	
	/**
	 * Loads a Walnut-format config from a String, and returns it.
	 * <p>
	 * The resulting config will use the passed config as it's defaults.
	 * 
	 * @param wlnt a properly formatted Walnut config string
	 * @return a newly created config as represented by the given string
	 * @throws ParseException if the contents of the string are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromString(String wlnt) throws ParseException {
		return fromString(wlnt, null);
	}
	
	/**
	 * Loads a Walnut-format config from a String, and returns it.
	 * <p>
	 * The resulting config will have no defaults.
	 * 
	 * @param wlnt a properly formatted Walnut config string
	 * @return a newly created config as represented by the given string
	 * @throws ParseException if the contents of the string are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromString(String wlnt, WalnutConfig defaults) throws ParseException {
		try {
			return fromReader(new StringReader(wlnt), defaults, true);
		} catch (IOException e) {
			throw (ParseException)new ParseException("Unexpected IOException from StringReader", 0).initCause(e);
		}
	}
	
	
	/**
	 * Loads a Walnut-format config from a File, and returns it.
	 * <p>
	 * The resulting config will have no defaults.
	 * If the file does not exist, an exception will be thrown.
	 * 
	 * @param file a path to a file, whose contents are a properly formatted UTF-8 Walnut config
	 * @return a newly created config as represented by the contents of the given File
	 * @throws IOException if an IO error occurs on an underlying stream
	 * @throws ParseException if the contents of the file are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromFile(File file) throws IOException, ParseException {
		return fromFile(file, null, false);
	}
	
	/**
	 * Loads a Walnut-format config from a File, and returns it.
	 * <p>
	 * The resulting config will use the passed config as it's defaults.
	 * If the file does not exist, an exception will be thrown.
	 * 
	 * @param file a path to a file, whose contents are a properly formatted UTF-8 Walnut config
	 * @return a newly created config as represented by the contents of the given File
	 * @throws IOException if an IO error occurs on an underlying stream
	 * @throws ParseException if the contents of the file are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromFile(File file, WalnutConfig defaults) throws IOException, ParseException {
		return fromFile(file, defaults, false);
	}
	
	/**
	 * Loads a Walnut-format config from a File, and returns it.
	 * <p>
	 * The resulting config will use the passed config as it's defaults.
	 * 
	 * If the file does not exist, and {@code writeDefaults} is true, it will be created with the serialized
	 * contents of {@code defaults}. If creating the file or writing {@code defaults} fails, an exception
	 * will be thrown.
	 * 
	 * If {@code writeDefaults} is false, behaves identically to {@link #fromFile(File, WalnutConfig)}.
	 * 
	 * @param file a path to a file, whose contents are a properly formatted UTF-8 Walnut config
	 * @param defaults a config containing default values, which are to be used if a mapping is missing
	 * @param writeDefaults true to automatically create a file based on the {@code defaults} if {@code f}
	 * 				does not exist
	 * @return a newly created config as represented by the contents of the given File
	 * @throws IOException if an IO error occurs on an underlying stream
	 * @throws ParseException if the contents of the file are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromFile(File file, WalnutConfig defaults, boolean writeDefaults) throws IOException, ParseException {
		if (file.exists()) {
			return fromStream(new FileInputStream(file), defaults, true);
		} else {
			if (writeDefaults) {
				//defaults.toFile(file); TODO
				return defaults.clone();
			} else {
				throw new FileNotFoundException();
			}
		}
	}
	
	
	/**
	 * Loads a Walnut-format config from the classpath, and returns it.
	 * <p>
	 * The resulting config will have no defaults.
	 * <p>
	 * This behaves as if:
	 * <pre>
	 * 	WalnutConfig.fromStream(ClassLoader.getSystemResourceAsStream(path), true);
	 * </pre>
	 * 
	 * @param path a path to a resource on the classpath, as in {@link ClassLoader#getResource(String)}
	 * @return a newly created config as represented by the contents of the classpath file
	 * @throws IOException if an IO error occurs on an underlying stream
	 * @throws ParseException if the contents of the file are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromClasspath(String path) throws IOException, ParseException {
		return fromClasspath(path, null);
	}
	
	/**
	 * Loads a Walnut-format config from the classpath, and returns it.
	 * <p>
	 * The resulting config will use the passed config as it's defaults.
	 * <p>
	 * This behaves as if:
	 * <pre>
	 * 	WalnutConfig.fromStream(ClassLoader.getSystemResourceAsStream(path), defaults, true);
	 * </pre>
	 * 
	 * @param path a path to a resource on the classpath, as in {@link ClassLoader#getResource(String)}
	 * @param defaults a config containing default values, which are to be used if a mapping is missing
	 * @return a newly created config as represented by the contents of the classpath file
	 * @throws IOException if an IO error occurs on an underlying stream
	 * @throws ParseException if the contents of the file are not syntatically correct Walnut
	 * @since 0.0.1
	 */
	public static WalnutConfig fromClasspath(String path, WalnutConfig defaults) throws IOException, ParseException {
		return fromStream(ClassLoader.getSystemResourceAsStream(path), defaults, true);
	}
	
	
	
	public static WalnutConfig fromUrl(URL url) throws IOException, ParseException {
		return fromUrl(url, null);
	}
	
	public static WalnutConfig fromUrl(URL url, WalnutConfig defaults) throws IOException, ParseException {
		return fromStream(url.openStream(), defaults, true);
	}
	
	
	
	public static WalnutConfig fromStream(InputStream in) throws IOException, ParseException {
		return fromStream(in, null, false);
	}
	
	public static WalnutConfig fromStream(InputStream in, WalnutConfig defaults) throws IOException, ParseException {
		return fromStream(in, defaults, false);
	}
	
	public static WalnutConfig fromStream(InputStream in, WalnutConfig defaults, boolean close) throws IOException, ParseException {
		return fromReader(new InputStreamReader(in, "UTF-8"), defaults, close);
	}
	
	
	
	public static WalnutConfig fromReader(Reader r) throws IOException, ParseException {
		return fromReader(r, null, false);
	}
	
	public static WalnutConfig fromReader(Reader r, WalnutConfig defaults) throws IOException, ParseException {
		return fromReader(r, defaults, false);
	}
	
	public static WalnutConfig fromReader(Reader r, WalnutConfig defaults, boolean close) throws IOException, ParseException {
		return new ConfigParser(r).prepare().parse();
	}


}
