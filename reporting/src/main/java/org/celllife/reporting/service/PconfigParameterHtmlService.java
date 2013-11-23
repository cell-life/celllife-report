package org.celllife.reporting.service;

import java.util.Enumeration;
import java.util.Map;

import org.celllife.pconfig.model.Pconfig;

public interface PconfigParameterHtmlService {

    public String createHtmlFieldsFromPconfig(Pconfig pconfig, String buttonId);

    public Pconfig createPconfigFromHtmlFormSubmission(Enumeration parameterNames, Map parameterMap, Pconfig pconfig);

}
