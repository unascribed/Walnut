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

import com.unascribed.walnut.value.Value;

/**
 * Represents a mapping of String keys to values of varying types.
 * <p>
 * Utility methods for loading are supplied in the form of the from* methods.
 * A WalnutConfig can also be created directly via a constructor to make an
 * empty config.
 * 
 * @since 0.0.1
 */
public class WalnutConfig implements Cloneable {
	protected Map<String, Value> map = new HashMap<String, Value>();
	
	////////// INSTANCE
	
	@Override
	public WalnutConfig clone() {
		WalnutConfig o;
		try {
			o = (WalnutConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
		o.map = new HashMap<String, Value>();
		for (Map.Entry<String, Value> en : map.entrySet()) {
			o.map.put(en.getKey(), en.getValue().clone());
		}
		return o;
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
		return null;
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
