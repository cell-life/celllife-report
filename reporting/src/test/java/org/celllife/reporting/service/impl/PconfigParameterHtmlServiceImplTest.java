package org.celllife.reporting.service.impl;

import junit.framework.Assert;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.celllife.pconfig.model.*;
import org.codehaus.groovy.util.ArrayIterator;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        BooleanParameter booleanParameter = new BooleanParameter("pregnant", "Check if pregnant.");
        booleanParameter.setValue(false);
        pconfig.addParameter(booleanParameter);

        BooleanParameter booleanParameter2 = new BooleanParameter("hivpositive", "Check if HIV Positive.");
        booleanParameter2.setValue(true);
        pconfig.addParameter(booleanParameter2);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(new Date());

        String expectedString = "<ol>" +
                "<li><label for=country>Country of Residence:</label><input id=country name=country value=\"South Africa\" type=text></li>" +
                "<li><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren value=10 type=number></li>" +
                "<li><label for=date>The date:</label><input id=date name=date value=\"" + reportDate + "\" type=date></li>" +
                "<li><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></li>" +
                "<li><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox checked></li>" +
                "</ol>";

        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createHtmlFieldsFromPconfig(pconfig));

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

        String expectedString = "<ol>" +
                "<li><label for=country>Country of Residence:</label><input id=country name=country type=text></li>" +
                "<li><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren type=number></li>" +
                "<li><label for=date>The date:</label><input id=date name=date type=date></li>" +
                "<li><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></li>" +
                "<li><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox></li>" +
                "</ol>";

        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createHtmlFieldsFromPconfig(pconfig));

    }

    @Test
    public void testCreatePconfigFromHtmlFormSubmission() {

        Map parameterMap = new HashMap<>();
        parameterMap.put("country", "South Africa");
        parameterMap.put("name", "John Smith");
        parameterMap.put("numberOfChildren", 10);
        parameterMap.put("date", "2013-11-07");
        parameterMap.put("pregnant", "true");

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
