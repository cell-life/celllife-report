package org.celllife.reporting.service.impl;

import org.celllife.pconfig.model.*;
import org.celllife.reporting.service.PconfigParameterHtmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class PconfigParameterHtmlServiceImpl implements PconfigParameterHtmlService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Returns an html string for form fields.
     *
     * @param pconfig The Pconfig for the HTML needed.
     * @return A string of HTML form fields an labels.
     */
    public String createHtmlFieldsFromPconfig(Pconfig pconfig, String buttonId) {

        String html = "<h2>"+pconfig.getLabel()+"</h2><form role=\"form\">";
        String paramHtml = "";
        List<? extends Parameter<?>> parameters = pconfig.getParameters();

        if (parameters != null && !parameters.isEmpty()) {
            for (final Parameter<?> param : parameters) {
                if (!param.isHidden()) {
                    paramHtml = this.getField(param);
                    html = html.concat(paramHtml);
                }
            }
        }
        return html+ "<button type=\"submit\" class=\"btn btn-default\" id=\"" + buttonId + "\">Run Report</button></form>";
    }

    /**
     * Converts an Enumeration and a paramterMap (taken from HttpServletRequest) to a Pconfig.
     *
     * @param parameterNames Enumeration of parameter names from HTML form.
     * @param parameterMap   Map of parameters from HTML form.
     * @param pconfig        Pconfig to fill.
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Pconfig createPconfigFromHtmlFormSubmission(Enumeration parameterNames, Map parameterMap, Pconfig pconfig) {

        List<Parameter<?>> pconfigParameters = pconfig.getParameters();

        for (Parameter pconfigParameter : pconfigParameters) {

            String paramName = pconfigParameter.getName();
            Object[] parameterValues = (Object[]) parameterMap.get(paramName);
            Object parameterValue;
            if (parameterValues == null) {
                parameterValue = "";
            } else {
                parameterValue = parameterValues[0];
            }

            if (pconfigParameter.isHidden()) {
                // do nothing
            } else if (pconfigParameter instanceof LabelParameter) {
                // do nothing
            } else if (pconfigParameter instanceof StringParameter) {
                if (parameterValue.toString().isEmpty() && !pconfigParameter.isOptional()) {
                    throw new RuntimeException("The parameter " + paramName + " is required.");
                }
                ((StringParameter) pconfig.getParameter(paramName)).setValue((String) parameterValue);
            } else if (pconfigParameter instanceof IntegerParameter) {
                if (parameterValue.toString().isEmpty() && !pconfigParameter.isOptional()) {
                    throw new RuntimeException("The parameter " + paramName + " is required.");
                }
                ((IntegerParameter) pconfig.getParameter(paramName)).setValue((Integer) parameterValue);
            } else if (pconfigParameter instanceof DateParameter) {
                if (parameterValue.toString().isEmpty() && !pconfigParameter.isOptional()) {
                    throw new RuntimeException("The parameter " + paramName + " is required.");
                }
                String dateFormat = "yyyy-MM-dd";
                if (!parameterValue.toString().isEmpty()) {
                    try {
                        Date date = new SimpleDateFormat(dateFormat).parse((String) parameterValue);
                        ((DateParameter) pconfig.getParameter(paramName)).setValue(date);
                    } catch (ParseException e) {
                        throw new RuntimeException("The date " + parameterValue.toString() + " is invalid. The expected format is '" + dateFormat + "'.");
                    }
                }
            } else if (pconfigParameter instanceof BooleanParameter) {
                ((BooleanParameter) pconfig.getParameter(paramName)).setValue(Boolean.parseBoolean((String) parameterValue));
            } else if (pconfigParameter instanceof SelectParameter) {
                if (parameterValue.toString().isEmpty() && !pconfigParameter.isOptional()) {
                    throw new RuntimeException("The parameter " + paramName + " is required.");
                }
                ((SelectParameter) pconfig.getParameter(paramName)).setValue((String) parameterValue);
                try {
                    String fullname = ((SelectParameter) pconfig.getParameter(paramName)).getOptionName(((String) parameterValue));
                    ((StringParameter) pconfig.getParameter(paramName + "_name")).setValue(fullname);
                } catch (Exception e)   {
                    log.warn("Could not set full name for select parameter with value " + ((String) parameterValue) + ". Possibly the .xml or .jrxml file does not contain a parameter with name " + paramName + "_name.");
                }
            }
        }

        return pconfig;
    }

    private String getField(final Parameter<?> param) {

        String html = "";

        if (param instanceof StringParameter) {
            return getFieldHtmlForStringParameter(param);
        } else if (param instanceof IntegerParameter) {
            return getFieldHtmlForIntegerParameter(param);
        } else if (param instanceof DateParameter) {
            return getFieldHtmlForDateParameter(param);
        } else if (param instanceof BooleanParameter) {
            return getFieldHtmlForBooleanParameter(param);
        } else if (param instanceof SelectParameter) {
            return getFieldHtmlForSelectParameter(param);
        } else if (param instanceof LabelParameter) {
            return ("<div class=\"form-group\">" + param.getValue().toString() + "</div>");
        }

        return html;
    }

    /**
     * Returns html for a string/text field. The field will be in the format:
     * <label for=country>Country of Residence:</label>
     * <input id=country name=country value="South Africa" type=text>
     *
     * @param param The parameter to construct the field from.
     * @return A string of html.
     */
    private String getFieldHtmlForStringParameter(final Parameter<?> param) {

        final StringParameter stringParam = (StringParameter) param;
        String defaultValue = stringParam.getDefaultValue();

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (defaultValue == null ? "" : (" value=\"" + defaultValue.toString()) + "\"") +
                " type=text class=\"form-control\"></div>";

        return html;
    }

    /**
     * Returns html for an integer/number field. The field will be in the format:
     * <label for=numberofchildren>Number of children:</label>
     * <input id=numberofchildren name=numberofchildren value=10 type=number>
     *
     * @param param The parameter to construct the field from.
     * @return A string of html.
     */
    private String getFieldHtmlForIntegerParameter(final Parameter<?> param) {

        final IntegerParameter integerParam = (IntegerParameter) param;
        Integer defaultValue = integerParam.getDefaultValue();

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (defaultValue == null ? "" : " value=" + defaultValue) +
                " type=number class=\"form-control\"></div>";

        return html;
    }

    /**
     * Returns html for a date field. The field will be in the format:
     * <label for=date>The date:</label>
     * <input id=date name=date value="2013-11-07" type=date>
     * The HTML5 date input specification refers to the RFC3339 specification which specifies a full-date format equal to: yyyy-mm-dd.
     * See http://dev.w3.org/html5/markup/input.date.html for details.
     *
     * @param param The parameter to construct the field from.
     * @return A string of html.
     */
    private String getFieldHtmlForDateParameter(final Parameter<?> param) {

        final DateParameter dateParam = (DateParameter) param;
        Date defaultValue = dateParam.getDefaultValue();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (defaultValue == null ? "" : (" value=" + "\"" + df.format(defaultValue)) + "\"") +
                " placeholder=\"yyyy-mm-dd\"" +
                " type=date class=\"form-control\"></div>";
        return html;

    }

    /**
     * Returns html for a boolean field. The field will be in the format:
     * <label for=date>The date:</label>
     * <input id=date name=date value="2013-11-07" type=date>
     * The HTML5 date input specification refers to the RFC3339 specification which specifies a full-date format equal to: yyyy-mm-dd.
     * See http://dev.w3.org/html5/markup/input.date.html for details.
     *
     * @param param The parameter to construct the field from.
     * @return A string of html.
     */
    private String getFieldHtmlForBooleanParameter(final Parameter<?> param) {

        final BooleanParameter boolParam = (BooleanParameter) param;
        Boolean defaultValue = boolParam.getDefaultValue();

        String html = "<div class=\"checkbox\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                " value=\"true\""
                + " type=checkbox";

        if ((defaultValue != null) && ((Boolean) defaultValue == true)) {
            html = html.concat(" checked></div>");
        } else {
            html = html.concat("></div>");
        }

        return html;
    }

    protected String getFieldHtmlForSelectParameter(final Parameter<?> param) {

        final SelectParameter selectParam = (SelectParameter) param;
        String defaultValue = selectParam.getDefaultValue();

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<select id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (defaultValue == null ? "" : (" value=\"" + defaultValue.toString()) + "\"") +
                " class=\"form-control\">";

        for (SelectParameterOption option : selectParam.getOptions()) {
            html = html.concat("<option value=\"" + option.getValue() + "\">" + option.getName() + "</option>");
        }

        html = html.concat("</select></div>");

        return html;
    }

}
