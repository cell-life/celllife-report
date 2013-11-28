package org.celllife.pconfig.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Select Parameter
 */
@XmlType(name = "select")
@XmlAccessorType(XmlAccessType.FIELD)
public class SelectParameter extends BaseParameter<String> {

	private static final long serialVersionUID = 5063147282465811918L;

    @XmlElement(name="option")
    SelectParameterOption[] options;

    public SelectParameter() {

    }

    public SelectParameter(String name, String label) {
        super(name, label);
    }
	
	@Override
	@XmlAttribute
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	@XmlAttribute
	public String getDefaultValue() {
		return defaultValue;
	}
	
	@Override
	public void setDefaultValue(String valueIfNull) {
		this.defaultValue = valueIfNull;
	}

    public SelectParameterOption[] getOptions() {
        return options;
    }

    public void setOptions(SelectParameterOption[] options) {
        this.options = options;
    }

    public String getOptionName(String value) {

        for (SelectParameterOption selectParameterOption : options) {
            if (selectParameterOption.getValue().equals(value)) {
                return  selectParameterOption.getName();
            }
        }

        return null;
    }
}
