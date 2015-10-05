package com.unascribed.walnut.value;

public final class StringValue extends BaseValue<StringValue> {
	public final String value;

	public StringValue(String rawValue, String value) {
		super(rawValue);
		this.value = value;
	}
	
	@Override
	public StringValue clone() {
		return new StringValue(rawValue, value);
	}
	
	@Override
	protected boolean valuesEqual(StringValue that) {
		if (that.value == null) return this.value == null;
		return this.value.equals(that.value);
	}
}