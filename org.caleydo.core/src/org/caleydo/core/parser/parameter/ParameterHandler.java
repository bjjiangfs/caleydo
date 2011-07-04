package org.caleydo.core.parser.parameter;

import org.xml.sax.Attributes;

/**
 * Handles attributes from XML file used to create objects.
 * 
 * @author Michael Kalkusch
 */
public final class ParameterHandler {

	public enum ParameterHandlerType {

		BOOL(),
		INT(),
		FLOAT(),
		STRING(),
	}

	private ParameterKeyValueDataAndDefault<Integer> hashKey2Integer;

	private ParameterKeyValueDataAndDefault<Float> hashKey2Float;

	private ParameterKeyValueDataAndDefault<String> hashKey2String;

	private ParameterKeyValueDataAndDefault<Boolean> hashKey2Boolean;

	public ParameterHandler() {

		hashKey2Integer = new ParameterKeyValueDataAndDefault<Integer>();
		hashKey2Float = new ParameterKeyValueDataAndDefault<Float>();
		hashKey2String = new ParameterKeyValueDataAndDefault<String>();
		hashKey2Boolean = new ParameterKeyValueDataAndDefault<Boolean>();
	}

	public String getValueString(final String key) {

		return hashKey2String.getValue(key);
	}

	public Integer getValueInt(final String key) {

		return hashKey2Integer.getValue(key);
	}

	public void setValueAndTypeAndDefault(final String key, final String value,
		final ParameterHandlerType type, final String defaultValue) {

		try {
			switch (type) {
				case BOOL:
					try {
						hashKey2Boolean.setValueAndDefaultValue(key, Boolean.valueOf(value),
							Boolean.valueOf(defaultValue));
					}
					catch (NumberFormatException nfe) {
						hashKey2Boolean.setValueAndDefaultValue(key, Boolean.valueOf(defaultValue),
							Boolean.valueOf(defaultValue));
					}
					break;

				case FLOAT:
					try {
						hashKey2Float.setValueAndDefaultValue(key, Float.valueOf(value),
							Float.valueOf(defaultValue));
					}
					catch (NumberFormatException nfe) {
						hashKey2Float.setValueAndDefaultValue(key, Float.valueOf(defaultValue),
							Float.valueOf(defaultValue));
					}
					break;

				case INT:
					try {
						hashKey2Integer.setValueAndDefaultValue(key, Integer.valueOf(value),
							Integer.valueOf(defaultValue));
					}
					catch (NumberFormatException nfe) {
						hashKey2Integer.setValueAndDefaultValue(key, Integer.valueOf(defaultValue),
							Integer.valueOf(defaultValue));
					}
					break;

				case STRING:
					if (key.length() > 0) {
						hashKey2String.setValueAndDefaultValue(key, value, defaultValue);
					}
					else {
						hashKey2String.setValueAndDefaultValue(key, defaultValue, defaultValue);
					}
					break;

				default:
					throw new IllegalStateException("ParameterHandler.setValueAndType(" + key
						+ ") uses unregistered enumeration!");
			}
		}
		catch (NumberFormatException nfe) {
			new IllegalStateException("ParameterHandler.setValueAndTypeAndDefault(" + key + ","
				+ defaultValue + ") defaultValue was not valid due to enumeration type=" + type.toString()
				+ " !");

		}
	}
	
	public final void setValueBySaxAttributes(final Attributes attrs, final String key,
		final String sDefaultValue, final ParameterHandlerType type) {

		assert sDefaultValue != null : "default value must not be null!";

		String value = attrs.getValue(key);

		if (value == null) {
			setValueAndTypeAndDefault(key, sDefaultValue, type, sDefaultValue);
			return;
		}

		setValueAndTypeAndDefault(key, value, type, sDefaultValue);
	}
}
