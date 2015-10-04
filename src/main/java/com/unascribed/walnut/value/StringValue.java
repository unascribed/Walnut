package com.unascribed.walnut.value;

public final class StringValue extends Value {
	public final String value;

	public StringValue(String rawValue, String value) {
		super(rawValue);
		this.value = value;
	}
	
	@Override
	public StringValue clone() {
		return new StringValue(rawValue, value);
	}

}