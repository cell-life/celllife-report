package org.celllife.pconfig.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;

public class PconfigUtils {
	
	private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * @param value String value.
     * @param valueType The type of object.
     * @return Returns an object, determined by valueType, to the value of the string specified.
     */
	public static Object convertValue(String value, String valueType) {
		if (Integer.class.getSimpleName().equals(valueType)){
			return Integer.valueOf(value);
		} else if (Long.class.getSimpleName().equals(valueType)){
			return Long.valueOf(value);
		} else if (Boolean.class.getSimpleName().equals(valueType)){
			return Boolean.valueOf(value);
		} else if (Double.class.getSimpleName().equals(valueType)){
			return Double.valueOf(value);
		} else {
			return value;
		}
	}

    /**
     * Converts a date to a string object.
     * @param date
     * @return Date converted to String format.
     */
	public static String dateToString(Date date){
		return format.format(date);
	}
	
	public static Date stringToDate(String dateString){
		try {
			return format.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * This method takes the parameter values from a parameter list and inserts them into the Pconfig.
	 * @param descriptor
	 *            the Pconfig to place the parameters in.
	 * @param parameterList
	 *            the list of Parameter objects to place in the Pconfig
	 */
	public static void merge(Pconfig descriptor, List<Parameter<?>> parameterList) {
		if (parameterList == null || parameterList.isEmpty()){
			return;
		}
		
		for (Parameter<?> param : parameterList) {
			Parameter<?> p1 = descriptor.getParameter(param.getName());
			if (p1 != null){
				merge(param, p1);
			}
		}
	}

	/**
	 * This method copies the value from one {@link Parameter} to the other.
	 * Note: both parameters must be of the same type.
	 * @param withValue parameter to copy the value from
	 * @param template parameter to copy the value to
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void merge(Parameter<?> withValue, Parameter<?> template) {
		if (template.getClass().equals(withValue.getClass())){
			((Parameter)template).setValue(withValue.getValue());
			if (withValue instanceof EntityParameter){
				EntityParameter eparam = (EntityParameter) withValue;
				EntityParameter ep1 = (EntityParameter) template;
				ep1.setValueLabel(eparam.getValueLabel());
			}
		}
	}
}
