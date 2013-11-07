package org.celllife.reporting.service.impl;

import org.celllife.pconfig.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PconfigParameterHtmlServiceImpl {

    /** Returns an html string for form fields.
     * @param pconfig The Pconfig for the HTML needed.
     * @return A string of HTML form fields an labels.
     */
    public String createFieldsOnForm(Pconfig pconfig) {

        String html = "<ol>";
        String paramHtml = "";
        List<? extends Parameter<?>> parameters = pconfig.getParameters();

        if (parameters == null || parameters.isEmpty()) {
            return "This form is empty.";
        } else {
            for (final Parameter<?> param : parameters) {
                if (!param.isHidden()) {
                    paramHtml = this.getField(param);
                    html = html.concat("<li>" + paramHtml + "</li>");
                }
            }
        }
        return html+"</ol>";
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

        String html = "<label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (value == null ? "" : (" value=\"" + value.toString()) + "\"") +
                " type=text>";

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

        String html = "<label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (value == null ? "" : " value=" + value) +
                " type=number>";

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

        String html = "<label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                (dateValue == null ? "" : (" value=" + "\"" + df.format(dateValue)) + "\"") +
                " type=date>";
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

        String html = "<label for=" + param.getName().toLowerCase() + ">" +
                param.getLabel() + "</label>" +
                "<input id=" + param.getName().toLowerCase() +
                " name=" + param.getName().toLowerCase() +
                " value=\"true\""
                + " type=checkbox";

        if ((value != null) && ((Boolean) value == true)) {
            html = html.concat(" checked>");
        } else {
            html = html.concat(">");
        }

        return html;
    }

}
