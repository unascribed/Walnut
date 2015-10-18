package com.unascribed.walnut.value;

public final class DoubleValue extends BaseValue<DoubleValue> {
	public final double value;
	public DoubleValue(String rawValue, double value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public DoubleValue clone() {
		return new DoubleValue(rawValue, value);
	}

	@Override
	protected boolean valuesEqual(DoubleValue that) {
		// XXX should this use an epsilon?
		return that.value == this.value;
	}

	@Override
	public Double get() {
		return value;
	}
	
}
