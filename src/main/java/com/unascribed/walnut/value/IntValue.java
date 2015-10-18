package com.unascribed.walnut.value;

public final class IntValue extends BaseValue<IntValue> {
	public final int value;
	public IntValue(String rawValue, int value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public IntValue clone() {
		return new IntValue(rawValue, value);
	}

	@Override
	protected boolean valuesEqual(IntValue that) {
		return that.value == this.value;
	}
	
	@Override
	public Integer get() {
		return value;
	}

}
