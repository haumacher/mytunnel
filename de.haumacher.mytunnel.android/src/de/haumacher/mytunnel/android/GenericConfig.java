package de.haumacher.mytunnel.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class GenericConfig implements Comparable<GenericConfig> {
	
	static abstract class Property implements Comparable<Property> {

		private final String _name;

		public Property(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public abstract Object getValue();
		
		protected final String loadValue(SharedPreferences preferences, String prefix) {
			return loadValue(preferences, getKey(prefix), null);
		}
		
		protected final String loadValue(SharedPreferences preferences, String key, String defaultValue) {
			try {
				return preferences.getString(key, defaultValue);
			} catch (ClassCastException ex) {
				return null;
			}
		}

		public abstract void load(SharedPreferences preferences, String prefix);

		public String getKey(String prefix) {
			return prefix + '.' + getName();
		}

		public abstract void store(Editor editor, String prefix);

		public void delete(Editor editor, String prefix) {
			editor.remove(getKey(prefix));
		}

	}
	
	static class StringProperty extends Property {

		private String value;
		
		public StringProperty(String name) {
			super(name);
		}
		
		@Override
		public String getValue() {
			return getStringValue();
		}

		private String getStringValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public void load(SharedPreferences preferences, String prefix) {
			setValue(loadValue(preferences, getKey(prefix), getValue()));
		}

		@Override
		public void store(Editor editor, String prefix) {
			editor.putString(getKey(prefix), getValue());
		}

		@Override
		public int compareTo(Property another) {
			return getValue().compareTo(((StringProperty) another).getStringValue());
		}

	}
	
	static class IntProperty extends Property {
		
		private int _value;
		
		public IntProperty(String name) {
			super(name);
		}
		
		@Override
		public Integer getValue() {
			return getIntValue();
		}

		private int getIntValue() {
			return _value;
		}
		
		public void setValue(int value) {
			_value = value;
		}

		@Override
		public void load(SharedPreferences preferences, String prefix) {
			String valueString = loadValue(preferences, prefix);
			if (valueString != null) {
				setValue(Integer.parseInt(valueString));
			}
		}

		@Override
		public void store(Editor editor, String prefix) {
		    editor.putString(getKey(prefix), getValueString());
		}

		public String getValueString() {
			return Integer.toString(getValue());
		}
		
		@Override
		public int compareTo(Property another) {
			return compare(getIntValue(), ((IntProperty) another).getIntValue());
		}

		private int compare(int x1, int x2) {
			return x1 < x2 ? -1 : x1 > x2 ? 1 : 0;
		}

	}
	
	private final String _id;
	
	private final Property[] _properties;
	
	public GenericConfig(String id, Property[] properties) {
		_id = id;
		_properties = properties;
	}
	
	public String getId() {
		return _id;
	}
	
	public GenericConfig load(SharedPreferences preferences) {
		String prefix = prefix();
		for (Property property : _properties) {
			property.load(preferences, prefix);
		}
		return this;
	}

	public void store(Editor editor) {
		String prefix = prefix();
		
		for (Property property : _properties) {
			property.store(editor, prefix);
		}
	}
	
	public void delete(Editor editor) {
		String prefix = prefix();
		for (Property property : _properties) {
			property.delete(editor, prefix);
		}
	}

	public String prefix() {
		return parentPrefix() + '.' + getId();
	}

	protected abstract String parentPrefix();

	@Override
	public int compareTo(GenericConfig other) {
		Property[] myProperties = _properties;
		Property[] otherProperties = other._properties;
		
		for (int n = 0, cnt = Math.min(myProperties.length, otherProperties.length); n< cnt; n++) {
			int result = myProperties[n].compareTo(otherProperties[n]);
			if (result != 0) {
				return result;
			}
		}
		
		return 0;
	}

}