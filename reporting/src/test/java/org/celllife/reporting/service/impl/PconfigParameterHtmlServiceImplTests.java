package org.celllife.reporting.service.impl;

import junit.framework.Assert;
import org.celllife.pconfig.model.*;
import org.junit.Test;

import java.util.Date;

public class PconfigParameterHtmlServiceImplTests {

    private PconfigParameterHtmlServiceImpl pconfigParameterHtmlService = new PconfigParameterHtmlServiceImpl();

    @Test
    public void testCreateFieldsOnForm() {

        Pconfig pconfig = new Pconfig();

        StringParameter stringParameter = new StringParameter("country","Country of Residence:");
        stringParameter.setValue("South Africa");
        pconfig.addParameter(stringParameter);

        IntegerParameter integerParameter = new IntegerParameter("numberOfChildren","Number of children:");
        integerParameter.setValue(10);
        pconfig.addParameter(integerParameter);

        DateParameter dateParameter = new DateParameter("date","The date:");
        dateParameter.setValue(new Date());
        pconfig.addParameter(dateParameter);

        BooleanParameter booleanParameter = new BooleanParameter("pregnant","Check if pregnant.");
        booleanParameter.setValue(false);
        pconfig.addParameter(booleanParameter);

        BooleanParameter booleanParameter2 = new BooleanParameter("hivpositive","Check if HIV Positive.");
        booleanParameter2.setValue(true);
        pconfig.addParameter(booleanParameter2);

        String expectedString = "<ol>" +
                "<li><label for=country>Country of Residence:</label><input id=country name=country value=\"South Africa\" type=text></li>" +
                "<li><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren value=10 type=number></li>" +
                "<li><label for=date>The date:</label><input id=date name=date value=\"2013-11-07\" type=date></li>" +
                "<li><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></li>" +
                "<li><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox checked></li>" +
                "</ol>";

        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createFieldsOnForm(pconfig));

    }

    @Test
    public void testCreateFieldsOnForm_ValuesEmpty() {

        Pconfig pconfig = new Pconfig();

        StringParameter stringParameter = new StringParameter("country","Country of Residence:");
        pconfig.addParameter(stringParameter);

        IntegerParameter integerParameter = new IntegerParameter("numberOfChildren","Number of children:");
        pconfig.addParameter(integerParameter);

        DateParameter dateParameter = new DateParameter("date","The date:");
        pconfig.addParameter(dateParameter);

        BooleanParameter booleanParameter = new BooleanParameter("pregnant","Check if pregnant.");
        pconfig.addParameter(booleanParameter);

        BooleanParameter booleanParameter2 = new BooleanParameter("hivpositive","Check if HIV Positive.");
        pconfig.addParameter(booleanParameter2);

        String expectedString = "<ol>" +
                "<li><label for=country>Country of Residence:</label><input id=country name=country type=text></li>" +
                "<li><label for=numberofchildren>Number of children:</label><input id=numberofchildren name=numberofchildren type=number></li>" +
                "<li><label for=date>The date:</label><input id=date name=date type=date></li>" +
                "<li><label for=pregnant>Check if pregnant.</label><input id=pregnant name=pregnant value=\"true\" type=checkbox></li>" +
                "<li><label for=hivpositive>Check if HIV Positive.</label><input id=hivpositive name=hivpositive value=\"true\" type=checkbox></li>" +
                "</ol>";

        Assert.assertEquals(expectedString, pconfigParameterHtmlService.createFieldsOnForm(pconfig));

    }


}
