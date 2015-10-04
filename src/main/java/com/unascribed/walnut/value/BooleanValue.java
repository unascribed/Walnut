package com.unascribed.walnut.value;

public final class BooleanValue extends Value {
	public final boolean value;
	public BooleanValue(String rawValue, boolean value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public BooleanValue clone() {
		return new BooleanValue(rawValue, value);
	}

}
