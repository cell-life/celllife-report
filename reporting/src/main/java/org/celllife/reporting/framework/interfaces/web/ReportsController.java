package org.celllife.reporting.framework.interfaces.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.celllife.pconfig.model.FileType;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.reporting.service.PconfigParameterHtmlService;
import org.celllife.reporting.service.ReportService;
import org.celllife.reporting.service.impl.PconfigParameterHtmlServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ReportsController {

    @Autowired
    private ReportService reportService;

    private PconfigParameterHtmlService pconfigParameterHtmlService = new PconfigParameterHtmlServiceImpl();

    //private static Logger log = LoggerFactory.getLogger(ReportsController.class);

    @ResponseBody
    @RequestMapping(
            value = "/service/reports",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Collection<Pconfig> getReports() {
        return reportService.getReports();
    }

    @ResponseBody
    @RequestMapping(value = "/service/getHtml", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlForReport(@RequestParam("reportId") String reportId) throws Exception {
        Pconfig pconfig;
        try {
            pconfig = reportService.getReportByName(reportId);
        } catch (Exception e) {
            return "No such Report.";
        }
        String htmlString = pconfigParameterHtmlService.createHtmlFieldsFromPconfig(pconfig, "submitButton");
        return htmlString;
    }

    @ResponseBody
    @RequestMapping(value = "/service/pdfReport", method = RequestMethod.GET, produces = "application/pdf")
    public void getPdfReport(HttpServletRequest request, HttpServletResponse response) throws Exception {

        generateReport(request, response, FileType.PDF);
    }

    @ResponseBody
    @RequestMapping(value = "/service/csvReport", method = RequestMethod.GET, produces = "application/csv")
    public void getCsvReport(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	generateReport(request, response, FileType.CSV);
    }
    
    @ResponseBody
    @RequestMapping(value = "/service/txtReport", method = RequestMethod.GET, produces = "application/txt")
    public void getTxtReport(HttpServletRequest request, HttpServletResponse response) throws Exception {

        generateReport(request, response, FileType.TXT);
    }

	private void generateReport(HttpServletRequest request, HttpServletResponse response, FileType reportFileType) {
		String reportId = request.getParameter("reportId");
        if (reportId.isEmpty()) {
            throw new RuntimeException("Could not retrieve a report with an empty reportId.");
        } else {

            Pconfig pconfig = reportService.getReportByName(reportId);
            Pconfig returnedPconfig = pconfigParameterHtmlService.createPconfigFromHtmlFormSubmission(
            		request.getParameterNames(), request.getParameterMap(), pconfig);

            String generatedReport = null;
            File reportFile = null;
            try {
                generatedReport = reportService.generateReport(returnedPconfig, reportFileType);
                reportFile = reportService.getGeneratedReportFile(generatedReport);
            } catch (Exception e) {
                throw new RuntimeException("Could not retrieve report with reportId '" + reportId + "'.", e);
            }
            if (reportFile == null) {
            	throw new RuntimeException("Could not retrieve report with reportId '" + reportId + "'.");
            }

            response.setContentType("application/"+reportFileType.getExtension().substring(1));
            response.setHeader("Content-Disposition", "attachment; filename=\"report-" + generatedReport + reportFileType.getExtension() + "\"");
            try {
                FileInputStream fileInputStream = new FileInputStream(reportFile);
                OutputStream responseOutputStream = response.getOutputStream();
                int bytes;
                while ((bytes = fileInputStream.read()) != -1) {
                    responseOutputStream.write(bytes);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create "+reportFileType.name()+" for report with reportId '"+reportId+"'.", e);
            }

        }
	}

}