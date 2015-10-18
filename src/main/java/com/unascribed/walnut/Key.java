package com.unascribed.walnut;

public class Key {
	protected String key;
	protected String documentation;
	public Key(String key, String documentation) {
		this.key = key;
		this.documentation = documentation;
	}
	public boolean hasDocumentation() {
		return documentation != null;
	}
	public String getDocumentation() {
		return documentation;
	}
	public String getKey() {
		return key;
	}
	@Override
	public String toString() {
		return key;
	}
	@Override
	public int hashCode() {
		return key == null ? 0 : key.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Key other = (Key) obj;
		return key.equals(other.key);
	}
}
