package com.unascribed.walnut.value;

public final class LongValue extends BaseValue<LongValue> {
	public final long value;
	public LongValue(String rawValue, long value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	public LongValue clone() {
		return new LongValue(rawValue, value);
	}
	
	@Override
	protected boolean valuesEqual(LongValue that) {
		return that.value == this.value;
	}

}
