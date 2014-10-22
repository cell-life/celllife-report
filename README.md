Celllife Reporting Framework
============================

This project provides Jasper reporting capabilities. Please see the documentation on the wiki: https://www.cell-life.org/confluence/display/CLS/Reporting+framework

To use
------

### Step 1: 

Add the celllife-mail dependency in your pom.xml. 

*root pom.xml*:

```xml
<jasperreports.version>5.5.0</jasperreports.version>
```

Note: Please check for the latest releases on [Nexus for celllife-report](https://www.cell-life.org/nexus/content/repositories/releases/org/celllife/report/) and the [Maven repository for Jasper](http://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports)

```xml
<!-- Cell-Life reporting framework -->
<dependency>
    <groupId>org.celllife.report</groupId>
    <artifactId>celllife-report-pconfig</artifactId>
    <version>1.0.2</version>
</dependency>
<dependency>
    <groupId>org.celllife.report</groupId>
    <artifactId>celllife-report-reporting</artifactId>
    <version>1.0.2</version>
</dependency>
<!-- Jasper -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>${jasperreports.version}</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
        <exclusion>
            <groupId>eclipse</groupId>
            <artifactId>jdtcore</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- Jasper Fonts -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports-fonts</artifactId>
    <version>${jasperreports.version}</version>
</dependency>
```

*webapp pom.xml*:

```xml
<!-- Cell-Life reporting framework -->
<dependency>
    <groupId>org.celllife.report</groupId>
    <artifactId>celllife-report-reporting</artifactId>
</dependency>
 
<dependency>
    <groupId>org.celllife.report</groupId>
    <artifactId>celllife-report-pconfig</artifactId>
</dependency>
 
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
</dependency>
```

### Step 2: 

Add the Jasper compile as part of the build process. The following XML needs to be added to the *webapp pom.xml* under the *plugins* section.

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jasperreports-maven-plugin</artifactId>
    <version>1.0-beta-2</version>
    <configuration>
        <sourceDirectory>src/main/resources/reports</sourceDirectory>
        <outputDirectory>${project.build.directory}/classes/reports
        </outputDirectory>
        <compiler>net.sf.jasperreports.compilers.JRGroovyCompiler</compiler>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile-reports</goal>
            </goals>
            <phase>compile</phase>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>net.sf.jasperreports</groupId>
            <artifactId>jasperreports</artifactId>
            <version>${jasperreports.version}</version>
        </dependency>
        <dependency>
            <groupId>net.sf.jasperreports</groupId>
            <artifactId>jasperreports-fonts</artifactId>
            <version>${jasperreports.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.0.10</version>
        </dependency>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-all</artifactId>
            <version>2.1.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
        </dependency>
    </dependencies>
</plugin>
```


### Step 3: 

Create a file called spring-reports.xml under your META-INF/spring folder. Please replace ${APP_HOME} with the env variable for your project. 

*example without scheduled reports email or sms functionality*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
 
    <bean id="reportServiceImpl"
          class="org.celllife.reporting.service.impl.JasperReportServiceImpl" init-method="buildService">
        <property name="dataSource" ref="dataSource" />
        <property name="generatedReportFolder" value="${APP_HOME}/reports/generated"/>
        <property name="scheduledReportFolder" value="${APP_HOME}/reports/scheduled"/>
        <property name="sourceReportFolder" value="classpath:reports"/>
        <property name="reportLoader">
            <bean class="org.celllife.reporting.service.impl.SpringResourceLoader"/>
        </property>
    </bean>
</beans>
```

*example with scheduled reports email functionality*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
 
    <bean id="reportServiceImpl"
          class="org.celllife.reporting.service.impl.JasperReportServiceImpl" init-method="buildService">
        <property name="dataSource" ref="dataSource" />
        <property name="generatedReportFolder" value="${OHSC_HOME}/reports/generated"/>
        <property name="scheduledReportFolder" value="${OHSC_HOME}/reports/scheduled"/>
        <property name="sourceReportFolder" value="classpath:reports"/>
        <property name="reportLoader">
            <bean class="org.celllife.reporting.service.impl.SpringResourceLoader"/>
        </property>
        <property name="mailService" ref="mailService" />
    </bean>
</beans>
```

Note: For the example above, you will also need to add a spring-mail.xml file as described in [step 2 option B in the celllife-mail dependency](https://www.cell-life.org/gitlab/celllife-mail/tree/master).

*example with scheduled reports email and sms functionality*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
 
    <bean id="reportServiceImpl"
          class="org.celllife.reporting.service.impl.JasperReportServiceImpl" init-method="buildService">
        <property name="dataSource" ref="dataSource" />
        <property name="generatedReportFolder" value="${OHSC_HOME}/reports/generated"/>
        <property name="scheduledReportFolder" value="${OHSC_HOME}/reports/scheduled"/>
        <property name="sourceReportFolder" value="classpath:reports"/>
        <property name="reportLoader">
            <bean class="org.celllife.reporting.service.impl.SpringResourceLoader"/>
        </property>
        <property name="mailService" ref="mailService" />
        <property name="communicateClient" ref="communicateClient" />
    </bean>
</beans>
```

Note: for the example above, you will also need to add a spring-integration-communicate.xml file as described below

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="communicateClient" class="org.celllife.mobilisr.client.impl.MobilisrClientImpl">
    	<constructor-arg index="0" value="${communicate.api}" />
    	<constructor-arg index="1" value="${communicate.username}" />
    	<constructor-arg index="2" value="${communicate.password}" />
    	<constructor-arg index="3"><ref bean="communicateValidatoryFactory" /></constructor-arg>
    </bean>
    
    <bean id="communicateValidatoryFactory" class="org.celllife.mobilisr.api.validation.ValidatorFactoryImpl">
    	<property name="countryRules">
    		<list>
    			<ref bean="communicateMsisdnRules"/>
    		</list>
    	</property>
    </bean>
    
    <bean id="communicateMsisdnRules" class="org.celllife.mobilisr.api.validation.MsisdnRule">
    	<constructor-arg name="name" value="southafrica"/>
    	<constructor-arg name="prefix" value="${communicate.msisdnPrefix}"/>
    	<constructor-arg name="validator" value="${communicate.msisdnRegex}"/>
    </bean>

</beans>
```

### Step 4: 

Add the properties referenced above into your application.properties

```bash
report.delete.cron=0 0 23 * * * 
report.scheduled.cron=0 0 9 * * *
```

If you are using email functionality, you also need to add these email properties.

```bash
mailSender.username=technical@cell-life.org
mailSender.password=xxxxxxxxxxxxx
mailSender.host=smtp.gmail.com
mailSender.protocol=smtps
mailSender.from=technical@cell-life.org
mailSender.port=465
mailSender.smtp.auth=false
mailSender.smtp.starttls.enable=false
```

If you are using sms functionality, you also need to add these email properties.

```bash
communicate.api=http://sol.cell-life.org/communicate
communicate.username=
communicate.password=
communicate.msisdnPrefix = 27
communicate.msisdnRegex = ^27[0-9]{9}$
```


### Step 5: 

Implement your reports and create a Pconfig XML file for each.

First, create a folder which contains your report resources under webapp/src/mail/resources/reports. This folder will contain your Jasper report files (.jrxml), any referenced images or styles and your Pconfig XML files which are used to describe your report so it can be loaded by this report library.

Now, create your Jasper reports (use iReport) and then create a Pconfig file to describe your report and its parameters.

*Example Pconfig file #1* - this file references a report named referrals_report.jrxml with two parameters (one date named "start_date" and one named "end_date"). These parameters need to match the parameters as defined in your jrxml.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<pconfig xmlns="http://www.cell-life.org/schemas/pconfig"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.cell-life.org/schemas/pconfig http://www.cell-life.org/schemas/pconfig/pconfig-1.0.xsd">
    <id>referrals</id>
    <label>Referrals</label>
    <resource>referrals_report.jasper</resource>
    <parameters>
        <!-- dates -->
        <date name="start_date" label="Start date" allowFuture="false" optional="true"/>
        <label value="If not set the start date will default to the beginning of the current month." />
        <date name="end_date" label="End date" allowFuture="false" optional="true"/>
        <label value="If not set the end date will default to the end of the current month." />
    </parameters>
</pconfig>
```

*More complex Pconfig file #2* - this file references a report named visits_report.jrxml with two date parameters, many boolean (checkbox) parameters and one drop down list single select parameter called "location_name". The parameter names all need to match the parameters in your jrxml.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<pconfig xmlns="http://www.cell-life.org/schemas/pconfig"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.cell-life.org/schemas/pconfig http://www.cell-life.org/schemas/pconfig/pconfig-1.0.xsd">
    <id>visits</id>
    <label>Visits</label>
    <resource>visits_report.jasper</resource>
    <parameters>
        <!-- dates -->
        <date name="start_date" label="Start date" allowFuture="false" optional="true"/>
        <label value="If not set the start date will default to the beginning of the current month." />
        <date name="end_date" label="End date" allowFuture="false" optional="true"/>
        <label value="If not set the end date will default to the end of the current month." />
        <!-- gender (female, male)-->
        <label label="Gender" value="Select gender" />
        <boolean label="Female" name="female" defaultValue="true" />
        <boolean label="Male" name="male" defaultValue="true" />
        <!-- age group (under 20, 21-40, 41-65, 65+) -->
        <label label="Age Group" value="Select age group" />
        <boolean label="Under 21" name="under21" defaultValue="true" />
        <boolean label="21-40" name="twenty1to40" defaultValue="true" />
        <boolean label="41-65" name="fourty1to65" defaultValue="true" />
        <boolean label="65+" name="over65" defaultValue="true" />
        <!-- martial status (single, married, divorced, widowed) -->
        <label label="Marital Status" value="Select maritial status" />
        <boolean label="Single" name="single" defaultValue="true" />
        <boolean label="Married" name="married" defaultValue="true" />
        <boolean label="Divorced" name="divorced" defaultValue="true" />
        <boolean label="Living together" name="living_together" defaultValue="true" />
        <boolean label="Widowed" name="widowed" defaultValue="true" />
        <!-- location -->
        <text name="location_name" optional="true" hidden="true"/>
        <select label="Location" name="location" defaultValue="%">
                <option><value>%</value><name>ALL LOCATIONS</name></option>
                <option>
                    <name>ATLANTIS</name>
                    <value>atlantis</value>
                </option>
                <option>
                    <value>bellv</value>
                    <name>BELLV</name>
                </option>
                <option><value>bishop_lavis</value><name>BISHOP LAVIS</name></option>
                <option><value>blue_d</value><name>BLUE D</name></option>
                <option><value>cape_town</value><name>CAPE TOWN</name></option>
                <option><value>delft</value><name>DELFT</name></option>
                <option><value>gugulethu</value><name>GUGULETHU</name></option>
                <option><value>joburg</value><name>JOHANNESBURG</name></option>
                <option><value>kuilsriv</value><name>KUILSRIV</name></option>
                <option><value>khayel</value><name>KHAYEL</name></option>
                <option><value>malmesbury</value><name>MALMESBURY</name></option>
                <option><value>mitchels_p</value><name>MITCHELS P</name></option>
                <option><value>muizen</value><name>MUIZEN</name></option>
                <option><value>nyanga</value><name>NYANGA</name></option>
                <option><value>paarl</value><name>PAARL</name></option>
                <option><value>phillipi</value><name>PHILLIPI</name></option>
                <option><value>pretoria</value><name>PRETORIA</name></option>
                <option><value>stellen</value><name>STELLEN</name></option>
                <option><value>welling</value><name>WELLING</name></option>
                <option><value>wynberg</value><name>WYNBERG</name></option>
        </select>
 
    </parameters>
</pconfig>
```

### Step 6: 

Implement the UI that allows the user to select a report, enter in the parameters and view the PDF or CSV.

#### A

First, you need to modify *spring-mvc.xml* in order to load the [ReportController](https://www.cell-life.org/gitlab/celllife-reports/tree/master/reporting/src/main/java/org/celllife/reporting/framework/interfaces/web/ReportsController.java) that provides the report REST interface.

```xml
<context:component-scan base-package="org.celllife.reporting.framework.interfaces.web"/>
```

The ReportController provides the following interface:
1. Get a list of the available reports (available at /service/reports)
2. Generate the HTML for the form used to retrieve the report parameters from the user running the report (available at /service/getHtml)
3. Generate a PDF report given the form submission (/service/pdfReport)
4. Generate a CSV report given the form submission (/service/csvReport)

#### B

Implement the Controller and JSP that uses ReportController. 

*IndexController.groovy* - provides a call to retrieve a list of all the reports available to the webapp. This is done by looking at the pconfig.xml files that have been created under the specified "sourceReportFolder" directory.

```groovy
package org.celllife.clinicfinder.interfaces.service
import org.celllife.clinicfinder.application.framework.restclient.RESTClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
@Controller
class IndexController {
    @Value('${external.base.url}')
    def String externalBaseUrl
     
    @Autowired
    RESTClient restClient
    @RequestMapping("/")
    def index(Model model) {
        getReports(model)
    }

    @RequestMapping(value="index", method = RequestMethod.GET)
    def getReports(Model model) {
        def reports = restClient.get("${externalBaseUrl}/service/reports")
        model.put("reports", reports)
        return "index";
    }
}
```

*index.jsp (using JQuery)* - displays the list of reports that have been retrieved by the IndexController and then when the user clicks on the link, retrieve the HTML for the parameter form and once that form has been submitted, retrieves a CSV report.

Note: Please use the JSP header/footer as defined by your application - this is just an example.

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>My app</title>
 
    <c:set var="url">${pageContext.request.requestURL}</c:set>
    <base href="${fn:substring(url, 0, fn:length(url) - fn:length(pageContext.request.requestURI))}${pageContext.request.contextPath}/" />
 
    <link href="resources/css/bootstrap-3.0.2.css" rel="stylesheet" media="screen">
    <link href="resources/css/bootstrap-theme-3.0.2.css" rel="stylesheet">
 
    <script type="text/javascript" src="resources/js/jquery-1.8.2.js"></script>
    <script type="text/javascript" src="resources/js/jquery-ui-1.9.1.min.js"></script>
    <script type="text/javascript" src="resources/js/bootstrap-3.0.2.js"></script>
</head>
<body>
 
<div class="container">
 
    <div class="masthead">
        <ul class="nav nav-pills pull-right">
            <li class="active"><a href="#">Home</a></li>
            <li><a href="j_spring_cas_security_logout">Logout</a>
        </ul>
         <h2><img src="resources/img/logo.png"></h2>
        <h3 class="muted">My app</h3>
    </div>
 
    <hr>
 
    <h3>Select a Report</h3>
    <br>
 
    <div class="row">
 
        <div class="col-md-4">
 
            <c:forEach items="${reports}" var="report">
                <a href="#${report.id}" class="active reportLink">
                    <h4>${report.label}</h4>
                </a>
            </c:forEach>
 
        </div>
 
        <div class="col-md-8 form">
 
        </div>
    </div>
 
    <hr>
 
    <div class="footer">
        <p>&copy; Cell-Life (NPO) - 2013</p>
    </div>
 
</div>
 
<script>
    $(document).ready(function () {
        $(".reportLink").click(function () {
            var reportId = $(this).attr('href') + '';
            reportId = reportId.replace('#', '');
            $.get("service/getHtml",
                    {reportId: reportId},
                    function (data) {
                        $(".form").html(data);
                        $(".form").on('submit', function (e) {
                            e.preventDefault();
                            window.location = "service/csvReport" + "?reportId=" + reportId + "&" + $("form").serialize();
                        });
                    }
            );
        });
    });
</script>
 
</body>
</html>
```
