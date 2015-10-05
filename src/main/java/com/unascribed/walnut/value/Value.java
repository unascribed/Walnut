package com.unascribed.walnut.value;

public interface Value {
	String getRawValue();
	Value clone();
	boolean equalsIgnoreRaw(Value v);
	boolean equals(Object o);
}

abstract class BaseValue<T extends BaseValue<?>> implements Value {
	protected final String rawValue;
	public BaseValue(String rawValue) {
		this.rawValue = rawValue;
	}
	@SuppressWarnings("unchecked")
	public boolean equalsIgnoreRaw(Value v) {
		if (v == null) return false;
		return v.getClass() == this.getClass() && valuesEqual((T)v);
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		T that = (T)obj;
		return (this.rawValue == null ? that.rawValue == null : this.rawValue.equals(that.rawValue)) && valuesEqual((T)obj);
	}
	protected abstract boolean valuesEqual(T that);
	public abstract T clone();
	@Override
	public String getRawValue() {
		return rawValue;
	}
	@Override
	public String toString() {
		return rawValue;
	}
}