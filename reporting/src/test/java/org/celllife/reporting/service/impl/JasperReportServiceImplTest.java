package org.celllife.reporting.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.client.MobilisrClient;
import org.celllife.mobilisr.client.impl.MobilisrClientImpl;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.FileType;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.RepeatInterval;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.celllife.pconfig.model.StringParameter;
import org.celllife.reporting.ReportingException;
import org.celllife.utilities.mail.MailServiceImpl;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class JasperReportServiceImplTest {

    private JasperReportServiceImpl service;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void setup() throws ReportingException {
        service = new JasperReportServiceImpl();
        service.setGeneratedReportFolder("target/generatedReports");
        service.setScheduledReportFolder("target/scheduledReports");
        service.setSourceReportFolder("target/reports/org/celllife/reporting/testreports");
        
        // mail service
        MailServiceImpl mailService = new MailServiceImpl();
        mailService.setFrom("technical@cell-life.org");
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        mailService.setMailSender(javaMailSender);
        javaMailSender.setUsername("technical@cell-life.org");
        javaMailSender.setPassword("kjasdf09u");
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setProtocol("smtps");
        javaMailSender.setPort(465);
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "false");
        props.setProperty("mail.smtp.starttls.enable", "false");
        javaMailSender.setJavaMailProperties(props);
        service.setMailService(mailService);
        
        // communicate
        List<MsisdnRule> rules = new ArrayList<MsisdnRule>();
        rules.add(new MsisdnRule("southafrica", "27", "^27[0-9]{9}$"));
        ValidatorFactory vfactory = new ValidatorFactoryImpl(rules);
        MobilisrClient communicateClient = new MobilisrClientImpl("http://sol.cell-life.org/communicate", 
                "username", "password", vfactory);
        service.setCommunicateClient(communicateClient);
        
        SpringResourceLoader loader = new SpringResourceLoader();
        loader.setResourceLoader(new FileSystemResourceLoader());
        service.setReportLoader(loader);
        service.buildService();
    }

    @Test
    public void testGetReports() {
        Collection<Pconfig> reports = service.getReports();
        Assert.assertEquals(8, reports.size());
    }

    @Test
    public void testGetReportByName() {
        String name = "report2";
        Pconfig report = service.getReportByName(name);
        Assert.assertEquals(name, report.getId());
    }

    @Test
    public void testGetReportsByProperty_nullValue() {
        String prop = "test-key1";
        Collection<Pconfig> reports = service.getReportsByProperty(prop, null);
        Assert.assertEquals(2, reports.size());
        for (Pconfig report : reports) {
            String name = report.getId();
            Assert.assertTrue(name.equals("report1") || name.equals("report2"));
        }
    }

    @Test
    public void testGetReportsByProperty_notNullValue() {
        String prop = "test-key1";
        String propVal = "value1";
        Collection<Pconfig> reports = service.getReportsByProperty(prop, propVal);
        Assert.assertEquals(1, reports.size());
        reports.iterator().next().getId().equals("report1");
    }

    @Test
    public void testGenerateReport() throws Exception {
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.XML);

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());

        validateReportFile(report.getPconfig(), reportFile);
    }
    
    @Test
    public void testGenerateTxtReport() throws Exception {
        Pconfig demo = service.getReportByName("demo_txt");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.TXT);

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());
        
        System.out.println("file="+reportFile);
        String txtFile = IOUtils.toString(new FileReader(reportFile));
        Assert.assertEquals("HELLO demo report", txtFile.trim());
    }

    @Test
    public void testGenerateCSVReport() throws Exception {
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.CSV);

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());
        BufferedReader reader = new BufferedReader(new FileReader(reportFile));
        Assert.assertEquals(",Demo report,,,", reader.readLine());
    }

    @Test
    public void testGenerateCSVReportLongStrings() throws Exception {
        Pconfig demo = service.getReportByName("demo_longstring");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.CSV);

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());
        BufferedReader reader = new BufferedReader(new FileReader(reportFile));
        Assert.assertEquals("Sms text", reader.readLine());
        Assert.assertEquals(
                "Testing a really long sms. what will happen when we try to export this to CSV as sms_text",
                reader.readLine());
    }

    @Test
    public void testGenerateReport_entity() throws Exception {
        Pconfig demo = service.getReportByName("entity_demo");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.XML);
        File generatedReportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(generatedReportFile.exists());

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());

        validateReportFile(report.getPconfig(), reportFile);
    }

    @Test
    public void testGenerateReport_entity_nullParams() throws Exception {
        Pconfig demo = service.getReportByName("entity_demo");
        String id = service.generateReport(demo, FileType.XML);
        File generatedReportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(generatedReportFile.exists());

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());

        validateReportFile(report.getPconfig(), reportFile);
    }

    @Test
    public void testGenerateReport_subreport() throws ReportingException, FileNotFoundException, IOException {
        Pconfig demo = service.getReportByName("demo_subreport");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.XML);
        File generatedReportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(generatedReportFile.exists());

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());

        String reportContents = IOUtils.toString(new FileInputStream(reportFile));
        Assert.assertTrue(reportContents.contains("unique report text only in subreport"));
    }

    @Test
    public void testDeleteOldReports() throws ReportingException {
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        String id1 = service.generateReport(demo, FileType.XML);
        File generatedReportFile1 = service.getGeneratedReportFile(id1);
        Assert.assertTrue(generatedReportFile1.exists());

        service.setMaxAge(-1);
        service.deleteOldReports();

        Assert.assertFalse(generatedReportFile1.exists());
    }

    @Test
    public void testGetGeneratedReports() throws ReportingException {
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        String id = service.generateReport(demo, FileType.XML);

        FilledPconfig report = service.getGeneratedReport(id);
        Assert.assertEquals(id, report.getId());
        File reportFile = service.getGeneratedReportFile(id);
        Assert.assertTrue(reportFile.exists());

        List<FilledPconfig> generatedReports = service.getGeneratedReports(demo.getId());
        Assert.assertEquals(1, generatedReports.size());
    }

    @Test
    public void testSaveScheduledReport() throws ReportingException {
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        ScheduledPconfig scheduledPconfig = new ScheduledPconfig(demo);
        scheduledPconfig.setEndDate(new Date());
        scheduledPconfig.setStartDate(new Date());
        scheduledPconfig.setIntervalCount(7);
        scheduledPconfig.setRepeatInterval(RepeatInterval.Daily);
        scheduledPconfig.setScheduledFor("test@test.com");

        List<ScheduledPconfig> scheduledReports = service.getScheduledReports(demo.getId());
        int count = scheduledReports.size();

        String id = service.saveScheduledReportConfig(scheduledPconfig);

        scheduledReports = service.getScheduledReports(demo.getId());
        Assert.assertEquals(count + 1, scheduledReports.size());

        ScheduledPconfig scheduledReport = service.getScheduledReport(id);
        Assert.assertEquals(scheduledPconfig.getId(), scheduledReport.getId());
        Assert.assertEquals(scheduledPconfig.getEndDate(), scheduledReport.getEndDate());
        Assert.assertEquals(scheduledPconfig.getStartDate(), scheduledReport.getStartDate());
        Assert.assertEquals(scheduledPconfig.getIntervalCount(), scheduledReport.getIntervalCount());
        Assert.assertEquals(scheduledPconfig.getRepeatInterval(), scheduledReport.getRepeatInterval());
        Assert.assertEquals(scheduledPconfig.getScheduledFor(), scheduledReport.getScheduledFor());
        
        service.deleteScheduledReport(id);
    }

    @Test
    public void testDeleteScheduledReport() throws ReportingException {
        Pconfig demo = service.getReportByName("demo");
        ScheduledPconfig scheduledPconfig = new ScheduledPconfig(demo);

        String id = service.saveScheduledReportConfig(scheduledPconfig);
        ScheduledPconfig scheduledReport = service.getScheduledReport(id);
        Assert.assertNotNull(scheduledReport);

        service.deleteScheduledReport(id);
        ScheduledPconfig deleted = service.getScheduledReport(id);
        Assert.assertNull(deleted);
    }

    @Test
    @Ignore("no Asserts - use to test the email sending when required.")
    public void testScheduledReport() throws Exception {
        
        Pconfig demo = service.getReportByName("demo");
        fillParameters(demo);
        ScheduledPconfig scheduledPconfig = new ScheduledPconfig(demo);
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.DAY_OF_MONTH, -3);
        scheduledPconfig.setStartDate(cal.getTime());
        cal.roll(Calendar.YEAR, 1);
        scheduledPconfig.setEndDate(cal.getTime());
        scheduledPconfig.setIntervalCount(1);
        scheduledPconfig.setRepeatInterval(RepeatInterval.Daily);
        scheduledPconfig.setFileType(FileType.PDF);
        scheduledPconfig.setScheduledFor("dagmar@cell-life.org");

        String id = service.saveScheduledReportConfig(scheduledPconfig);
        
        service.generateScheduledReports();
        Thread.sleep(15000);
        
        service.deleteScheduledReport(id);
    }

    @Test
    public void testCurrentIntervalMonthly() {
        JasperReportServiceImpl impl = new JasperReportServiceImpl();
        Date start = createDate(2014, Calendar.APRIL, 21, 13, 24, 43, 12);
        Date now = createDate(2014, Calendar.JULY, 21, 23, 59, 59, 999);
        int currentInterval = impl.getCurrentInterval(now, start, RepeatInterval.Monthly);
        Assert.assertEquals(4, currentInterval);
    }

    @Test
    public void testCurrentIntervalWeekly() {
        JasperReportServiceImpl impl = new JasperReportServiceImpl();
        Date start = createDate(2014, Calendar.APRIL, 21, 13, 24, 43, 12);
        Date now = createDate(2014, Calendar.JULY, 21, 23, 59, 59, 999);
        int currentInterval = impl.getCurrentInterval(now, start, RepeatInterval.Weekly);
        Assert.assertEquals(14, currentInterval);
    }

    @Test
    public void testCurrentIntervalDaily() {
        JasperReportServiceImpl impl = new JasperReportServiceImpl();
        Date start = createDate(2014, Calendar.APRIL, 21, 13, 24, 43, 12);
        Date now = createDate(2014, Calendar.JULY, 21, 23, 59, 59, 999);
        int currentInterval = impl.getCurrentInterval(now, start, RepeatInterval.Daily);
        Assert.assertEquals(92, currentInterval);
    }

    Date createDate(int year, int month, int day, int hour, int min, int sec, int millisec) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Sets test values for the report paramters
     */
    private void fillParameters(Pconfig report) {
        List<? extends Parameter<?>> parameters = report.getParameters();
        for (Parameter<?> param : parameters) {
            if (param instanceof StringParameter) {
                ((StringParameter) param).setValue("test string");
            } else if (param instanceof IntegerParameter) {
                ((IntegerParameter) param).setValue(13);
            } else if (param instanceof DateParameter) {
                ((DateParameter) param).setValue(new Date());
            } else if (param instanceof BooleanParameter) {
                ((BooleanParameter) param).setValue(true);
            } else if (param instanceof EntityParameter) {
                EntityParameter eparam = (EntityParameter) param;
                String type = eparam.getValueType();
                if (Integer.class.getSimpleName().equals(type)) {
                    eparam.setValue("21");
                } else if (Long.class.getSimpleName().equals(type)) {
                    eparam.setValue("42");
                }
                if (Double.class.getSimpleName().equals(type)) {
                    eparam.setValue("9.4");
                }
                if (Boolean.class.getSimpleName().equals(type)) {
                    eparam.setValue("false");
                }
            }
        }
    }

    /**
     * Checks that the XML report file contains the parameter values. It does
     * this by looking up Xpath expressions. The report file contains element
     * keys which locate the elements. These can be used to find the element
     * which should contain the value of a particular parameter.
     */
    private void validateReportFile(Pconfig report, File reportFile) throws Exception {
        System.out.println("VALIDATING "+reportFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //System.out.println(FileUtils.readFileToString(reportFile));

        Document doc = builder.parse(reportFile);

        XPathFactory xfactory = XPathFactory.newInstance();
        XPath xpath = xfactory.newXPath();

        for (Parameter<?> param : report.getParameters()) {
            String paramkey = param.getName() + "key";
            XPathExpression expr = xpath.compile("//text/reportElement[@key='" + paramkey + "']/../textContent");
            NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);

            Assert.assertEquals(1, nodes.getLength());

            Node item = nodes.item(0);            
            String textContent = item.getTextContent();
            Object value = param.getValue();
            if (value == null) {
                value = param.getDefaultValue();
            }

            if (param instanceof DateParameter) {
                Date date = ((DateParameter) param).getValue();
                value = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
            }
            collector.checkThat(value, IsNull.notNullValue());
            if (value != null) {
                collector.checkThat(textContent, IsEqual.equalTo(value.toString()));
            }
        }
    }

}
