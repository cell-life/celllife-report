package org.celllife.pconfig.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SelectParameterOption {

    String name;

    String value;

    public SelectParameterOption() {

    }

    public SelectParameterOption(String name, String id) {
        this.name = name;
        this.value = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
