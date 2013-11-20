package org.celllife.reporting.service.impl;

import junit.framework.Assert;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.celllife.pconfig.model.*;
import org.codehaus.groovy.util.ArrayIterator;
import org.junit.Ignore;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PconfigParameterHtmlServiceImplTest {

    private PconfigParameterHtmlServiceImpl pconfigParameterHtmlService = new PconfigParameterHtmlServiceImpl();

    @Test
    public void createHtmlFieldsFromPconfig() {

        Pconfig pconfig = new Pconfig();

        StringParameter stringParameter = new StringParameter("country", "Country of Residence:");
        stringParameter.setValue("South Africa");
        pconfig.addParameter(stringParameter);

        IntegerParameter integerParameter = new IntegerParameter("numberOfChildren", "Number of children:");
        integerParameter.setValue(10);
        pconfig.addParameter(integerParameter);

        DateParameter dateParameter = new DateParameter("date", "The date:");
        dateParameter.setValue(new Date());
        pconfig.addParameter(dateParameter);

        LabelParameter labelParameter = new LabelParameter();
        labelParameter.setValue("If not set the start date will default to the beginning of the current month.");
        pconfig.addParameter(labelParameter);

        BooleanParameter booleanParameter = new BooleanParameter("pregnant", "Check if pregnant.");
        booleanParameter.setValue(false);
        pconfig.addParameter(booleanParameter);

        BooleanParameter booleanParameter2 = new BooleanParameter("hivpositive", "Check if HIV Positive.");
        booleanParameter2.setValue(true);
        pconfig.addParameter(booleanParameter2);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String reportDate = df.format(new Date());

        String expectedString = "<form role=\"form\"><div class=\"form-group\"><label for=country>Country of Residence:</label><input id=country name=country value=\"South Africa\" type=text class=\"form-control\"></div>" +
                "<div class=\"form-group\"><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren value=10 type=number class=\"form-control\"></div>" +
                "<div class=\"form-group\"><label for=date>The date:</label><input id=date name=date value=\"" + reportDate + "\" type=date class=\"form-control\"></div>" +
                "<div class=\"form-group\">If not set the start date will default to the beginning of the current month.</div>" +
                "<div class=\"checkbox\"><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></div>" +
                "<div class=\"checkbox\"><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox checked></div>" +
                "<button type=\"submit\" class=\"btn btn-default\" id=\"submitButton\">Submit</button></form>";

        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createHtmlFieldsFromPconfig(pconfig, "submitButton"));

    }

    @Test
    public void testCreateHtmlFieldsFromPconfig_ValuesEmpty() {

        Pconfig pconfig = new Pconfig();

        StringParameter stringParameter = new StringParameter("country", "Country of Residence:");
        pconfig.addParameter(stringParameter);

        IntegerParameter integerParameter = new IntegerParameter("numberOfChildren", "Number of children:");
        pconfig.addParameter(integerParameter);

        DateParameter dateParameter = new DateParameter("date", "The date:");
        pconfig.addParameter(dateParameter);

        BooleanParameter booleanParameter = new BooleanParameter("pregnant", "Check if pregnant.");
        pconfig.addParameter(booleanParameter);

        BooleanParameter booleanParameter2 = new BooleanParameter("hivpositive", "Check if HIV Positive.");
        pconfig.addParameter(booleanParameter2);

        String expectedString = "<form role=\"form\"><div class=\"form-group\"><label for=country>Country of Residence:</label><input id=country name=country type=text class=\"form-control\"></div>" +
                "<div class=\"form-group\"><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren type=number class=\"form-control\"></div>" +
                "<div class=\"form-group\"><label for=date>The date:</label><input id=date name=date type=date class=\"form-control\"></div>" +
                "<div class=\"checkbox\"><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></div>" +
                "<div class=\"checkbox\"><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox></div>" +
                "<button type=\"submit\" class=\"btn btn-default\" id=\"submitButton\">Submit</button></form>";
        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createHtmlFieldsFromPconfig(pconfig, "submitButton"));

    }

    @Test
    public void testCreatePconfigFromHtmlFormSubmission() {

        Map parameterMap = new HashMap<>();
        Object[] objects = {"South Africa"};
        parameterMap.put("country", objects);
        objects = new Object[]{"John Smith"};
        parameterMap.put("name", objects);
        objects = new Object[]{10};
        parameterMap.put("numberOfChildren", objects);
        objects = new Object[]{"2013-11-07"};
        parameterMap.put("date", objects);
        objects = new Object[]{"true"};
        parameterMap.put("pregnant", objects);

        String[] strings = {"country", "name", "numberOfChildren", "date", "pregnant"};
        Iterator<String> stringIterator = new ArrayIterator<String>(strings);
        IteratorEnumeration parameterNames = new IteratorEnumeration(stringIterator);

        Pconfig pconfig = new Pconfig();
        StringParameter stringParameter = new StringParameter("country", "Country of Residence:");
        pconfig.addParameter(stringParameter);

        StringParameter stringParameter2 = new StringParameter("name", "Full Name:");
        pconfig.addParameter(stringParameter2);

        IntegerParameter integerParameter = new IntegerParameter("numberOfChildren", "Number of children:");
        pconfig.addParameter(integerParameter);

        DateParameter dateParameter = new DateParameter("date", "The date:");
        pconfig.addParameter(dateParameter);

        BooleanParameter booleanParameter = new BooleanParameter("pregnant", "Check if pregnant.");
        pconfig.addParameter(booleanParameter);

        Pconfig returnedPconfig = pconfigParameterHtmlService.createPconfigFromHtmlFormSubmission(parameterNames, parameterMap, pconfig);

        Assert.assertEquals("South Africa", returnedPconfig.getParameter("country").getValue());
        Assert.assertEquals("John Smith", returnedPconfig.getParameter("name").getValue());
        Assert.assertEquals(10, returnedPconfig.getParameter("numberOfChildren").getValue());
        Assert.assertEquals(true, returnedPconfig.getParameter("pregnant").getValue());

    }
}
