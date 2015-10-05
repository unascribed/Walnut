package com.unascribed.walnut.value;

import java.util.Arrays;

public class ArrayValue extends BaseValue<ArrayValue> {
	private final Value[] value;
	public ArrayValue(String rawValue, Value[] value) {
		super(rawValue);
		this.value = value;
	}

	@Override
	protected boolean valuesEqual(ArrayValue that) {
		return Arrays.deepEquals(value, that.value);
	}

	@Override
	public ArrayValue clone() {
		Value[] nw = new Value[value.length];
		for (int i = 0; i < value.length; i++) {
			nw[i] = value[i].clone();
		}
		return new ArrayValue(rawValue, nw);
	}

}
