package org.celllife.reporting.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.celllife.reporting.service.PconfigParameterHtmlService;

public class PconfigParameterHtmlServiceImpl implements PconfigParameterHtmlService {

    //private final Logger log = LoggerFactory.getLogger(getClass());

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
        return html+ "<button type=\"submit\" class=\"btn btn-default\" id=\"" + buttonId + "\">Submit</button></form>";
    }

    /**
     * Converts an Enumeration and a paramterMap (taken from HttpServletRequest) to a Pconfig.
     *
     * @param parameterNames Enumeration of parameter names from HTML form.
     * @param parameterMap   Map of parameters from HTML form.
     * @param pconfig        Pconfig to fill.
     * @return
     */
    public Pconfig createPconfigFromHtmlFormSubmission(Enumeration parameterNames, Map parameterMap, Pconfig pconfig) {

        while (parameterNames.hasMoreElements()) {

            String paramName = (String) parameterNames.nextElement();
            Object[]  parameterValues = (Object[])parameterMap.get(paramName);
            Object parameterValue = parameterValues[0];
            Object pconfigParameter = pconfig.getParameter(paramName);

            if (parameterValue.toString().isEmpty()) {
                throw new RuntimeException("The parameter " + paramName + " is null.");
            }

            if (pconfigParameter instanceof StringParameter) {
                ((StringParameter) pconfig.getParameter(paramName)).setValue((String) parameterValue);
            } else if (pconfigParameter instanceof IntegerParameter) {
                ((IntegerParameter) pconfig.getParameter(paramName)).setValue((Integer) parameterValue);
            } else if (pconfigParameter instanceof DateParameter) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse((String) parameterValue);
                    ((DateParameter) pconfig.getParameter(paramName)).setValue(date);
                } catch (ParseException e) {
                    throw new RuntimeException("The date " + parameterValue.toString() + " could not be parsed.");
                }
            } else if (pconfigParameter instanceof BooleanParameter) {
                ((BooleanParameter) pconfig.getParameter(paramName)).setValue(Boolean.parseBoolean((String) parameterValue));
            }
        }

        return pconfig;
    }

    private String getField(final Parameter<?> param) {

        Object value = null;
        String html = "";

        if (param instanceof StringParameter) {
            return getFieldHtmlForStringParameter(param);
        } else if (param instanceof IntegerParameter) {
            return getFieldHtmlForIntegerParameter(param);
        } else if (param instanceof DateParameter) {
            return getFieldHtmlForDateParameter(param);
        } else if (param instanceof BooleanParameter) {
            return getFieldHtmlForBooleanParameter(param);
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
        String value = stringParam.getValue();

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (value == null ? "" : (" value=\"" + value.toString()) + "\"") +
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
        Integer value = integerParam.getValue();

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (value == null ? "" : " value=" + value) +
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
        Date dateValue = dateParam.getValue();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String html = "<div class=\"form-group\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (dateValue == null ? "" : (" value=" + "\"" + df.format(dateValue)) + "\"") +
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
        Boolean value = boolParam.getValue();

        String html = "<div class=\"checkbox\"><label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                " value=\"true\""
                + " type=checkbox";

        if ((value != null) && ((Boolean) value == true)) {
            html = html.concat(" checked></div>");
        } else {
            html = html.concat("></div>");
        }

        return html;
    }

}
