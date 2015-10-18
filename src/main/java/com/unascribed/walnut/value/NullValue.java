package com.unascribed.walnut.value;

public class NullValue extends BaseValue<NullValue> {

	public NullValue(String rawValue) {
		super(rawValue);
	}

	@Override
	protected boolean valuesEqual(NullValue that) {
		return true;
	}

	@Override
	public NullValue clone() {
		return new NullValue(rawValue);
	}
	
	@Override
	public Object get() {
		return null;
	}

}
