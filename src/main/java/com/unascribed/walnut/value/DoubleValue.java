package com.unascribed.walnut.value;

public final class DoubleValue extends Value {
	public final double value;
	public DoubleValue(String rawValue, double value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public DoubleValue clone() {
		return new DoubleValue(rawValue, value);
	}

}
