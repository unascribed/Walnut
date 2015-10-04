package com.unascribed.walnut.value;

public final class IntValue extends Value {
	public final int value;
	public IntValue(String rawValue, int value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public IntValue clone() {
		return new IntValue(rawValue, value);
	}

}
