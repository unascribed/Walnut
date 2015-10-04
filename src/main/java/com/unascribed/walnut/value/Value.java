package com.unascribed.walnut.value;

public abstract class Value {
	public final String rawValue;
	public Value(String rawValue) {
		this.rawValue = rawValue;
	}
	public abstract Value clone();
}