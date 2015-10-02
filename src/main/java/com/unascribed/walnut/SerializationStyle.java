package com.unascribed.walnut;

/**
 * Defines style rules for serializing a {@link WalnutConfig}.
 * <p>
 * This includes such things as indentation, what key-value separator to use,
 * whether to align values with keys in the same section, etc.
 *
 * @since 0.0.1
 */
public final class SerializationStyle {
	
	////////// DEFAULT STYLES
	
	public static final SerializationStyle COLONS_TABS = builder().indentation("\t").useColons(true).build();
	
	public static final SerializationStyle COLONS_8SPACES = COLONS_TABS.withIndentation("        ");
	public static final SerializationStyle COLONS_4SPACES = COLONS_TABS.withIndentation("    ");
	public static final SerializationStyle COLONS_2SPACES = COLONS_TABS.withIndentation(" ");
	
	
	public static final SerializationStyle EQUALS_TABS = builder().indentation("\t").useColons(false).build();
	
	public static final SerializationStyle EQUALS_8SPACES = EQUALS_TABS.withIndentation("        ");
	public static final SerializationStyle EQUALS_4SPACES = EQUALS_TABS.withIndentation("    ");
	public static final SerializationStyle EQUALS_2SPACES = EQUALS_TABS.withIndentation(" ");
	
	////////// INSTANCE
	
	private final String indentation;
	private final boolean colons;
	private final boolean align;
	private final boolean omitSeparatorsForSections;
	
	private SerializationStyle(String indentation, boolean colons, boolean align, boolean omitSeparatorsForSections) {
		for (int i = 0; i < indentation.length(); i++) {
			if (!Character.isWhitespace(indentation.codePointAt(i))) {
				throw new IllegalArgumentException("indentation must be whitespace");
			}
		}
		this.indentation = indentation;
		this.colons = colons;
		this.align = align;
		this.omitSeparatorsForSections = omitSeparatorsForSections;
	}
	
	/**
	 * Create a new SerializationStyle, identical to this one, but with the passed indentation setting.
	 * 
	 * @param indentation the indentation to use for any files created using this style,
	 * 				being the prefix that is put before the keys in a section
	 * @return a newly created SerializationStyle, identical to this one, but with the passed
	 * 				indentation setting
	 * @since 0.0.1
	 */
	public SerializationStyle withIndentation(String indentation) {
		if (indentation == null) throw new IllegalArgumentException("indentation cannot be null");
		return new SerializationStyle(indentation, colons, align, omitSeparatorsForSections);
	}
	
	/**
	 * Create a new SerializationStyle, identical to this one, but with the passed colons setting.
	 * 
	 * @param colons whether to use colons or equals signs to separate keys and values, true
	 * 				meaning colons, false meaning equals signs
	 * @return a newly created SerializationStyle, identical to this one, but with the passed
	 * 				colons setting
	 * @since 0.0.1
	 */
	public SerializationStyle withColons(boolean colons) {
		return new SerializationStyle(indentation, colons, align, omitSeparatorsForSections);
	}
	
	/**
	 * Create a new SerializationStyle, identical to this one, but with the passed align values setting.
	 * <p>
	 * If true, files like the following will be generated:
	 * <pre>
	 * 	key:        [1, 2]
	 * 	long-key:   "value"
	 * 	longer-key: 54
	 * 	more-keys:  false
	 * </pre>
	 * If false, files like the following will be generated:
	 * <pre>
	 * 	key: [1, 2]
	 * 	long-key: "value"
	 * 	longer-key: 54
	 * 	more-keys: false
	 * </pre>
	 * 
	 * @param align whether or not to do value alignment
	 * @return a newly created SerializationStyle, identical to this one, but with the passed
	 * 				align setting
	 * @since 0.0.1
	 */
	public SerializationStyle withAlignValues(boolean align) {
		return new SerializationStyle(indentation, colons, align, omitSeparatorsForSections);
	}
	
	/**
	 * Create a new SerializationStyle, identical to this one, but with the passed omit setting.
	 * <p>
	 * If true, files like the following will be generated:
	 * <pre>
	 * 	section {
	 * 		some-key: "some value"
	 * 	}
	 * </pre>
	 * If false, files like the following will be generated:
	 * <pre>
	 * 	section: {
	 * 		some-key: "some value"
	 * 	}
	 * </pre>
	 * 
	 * @param omitSeparatorsForSections whether or not to omit seperators for sections
	 * @return a newly created SerializationStyle, identical to this one, but with the passed
	 * 				omit setting
	 * @since 0.0.1
	 */
	public SerializationStyle withOmitSeparatorsForSections(boolean omitSeparatorsForSections) {
		return new SerializationStyle(indentation, colons, align, omitSeparatorsForSections);
	}
	
	/////////// STATIC
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		private String indentation;
		private Boolean colons;
		private boolean align = false;
		private boolean omitSeparatorsForSections = true;
		
		public Builder indentation(String indentation) {
			this.indentation = indentation;
			return this;
		}
		public Builder alignValues(boolean align) {
			this.align = align;
			return this;
		}
		public Builder useColons(boolean colons) {
			this.colons = colons;
			return this;
		}
		public Builder omitSeparatorsForSections(boolean omitSeparatorsForSections) {
			this.omitSeparatorsForSections = omitSeparatorsForSections;
			return this;
		}
		
		public SerializationStyle build() {
			if (indentation == null || colons == null) {
				throw new IllegalStateException("At least identation and colons must be specified");
			}
			return new SerializationStyle(indentation, colons, align, omitSeparatorsForSections);
		}
		
	}
	
}
