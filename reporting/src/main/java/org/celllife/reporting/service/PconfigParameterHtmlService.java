package org.celllife.reporting.service;

import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;

import java.util.Enumeration;
import java.util.Map;

public interface PconfigParameterHtmlService {

    public String createHtmlFieldsFromPconfig(Pconfig pconfig, String buttonId);

    public Pconfig createPconfigFromHtmlFormSubmission(Enumeration parameterNames, Map parameterMap, Pconfig pconfig);

}
