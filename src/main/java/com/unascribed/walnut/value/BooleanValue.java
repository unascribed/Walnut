package com.unascribed.walnut.value;

public final class BooleanValue extends BaseValue<BooleanValue> {
	public final boolean value;
	public BooleanValue(String rawValue, boolean value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public BooleanValue clone() {
		return new BooleanValue(rawValue, value);
	}
	
	@Override
	protected boolean valuesEqual(BooleanValue that) {
		return that.value == this.value;
	}

}
