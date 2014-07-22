package org.celllife.reporting.service;

import java.io.File;
import java.util.List;

import org.celllife.pconfig.model.FileType;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.celllife.reporting.ReportingException;

/**
 * Provides all the necessary services to generate and schedule a report.
 */
public interface ReportService {

    /**
     * Gets a list of all the available reports
     * 
     * @return List<Pconfig>, not null
     */
    List<Pconfig> getReports();

    /**
     * Finds a report given a name/identifier
     * 
     * @param name
     * @return
     */
    Pconfig getReportByName(String name);

    /**
     * Get all reports with the given property.
     * 
     * @param propertyName the name of the property.
     * @param value optional parameter to also match the property value. Leave null to ignore.
     * @return
     */
    List<Pconfig> getReportsByProperty(String propertyName, String value);

    /**
     * Generate a report as a PDF.
     * 
     * @param Pconfig report containing all the report parameters selected by the user
     * @return String the generated report id
     * @throws ReportingException
     */
    String generateReport(Pconfig report) throws ReportingException;

    /**
     * Generate a report and specify the type of file that should be generated.
     * 
     * @param report Pconfig containing the report details and user selected parameters
     * @param type FileType indicating CSV, PDF, etc
     * @return String generated report id
     * @throws ReportingException
     */
    String generateReport(Pconfig report, FileType type) throws ReportingException;

    /**
     * Gets all the reports that have been recently generated by the server
     * 
     * @return a list of generated reports, will not be null
     */
    List<FilledPconfig> getGeneratedReports(String name);

    /**
     * Gets a specific report
     * 
     * @param reportId String name/identifier of the report
     * @return FilledPconfig, null if no report exists
     */
    FilledPconfig getGeneratedReport(String reportId);

    /**
     * Opens a file for a previously generated report.
     * 
     * @param reportId String name/identifier of the report
     * @return the report file or null if the file or report doesn't exist
     */
    File getGeneratedReportFile(String reportId);

    /**
     * Saves a scheduled report - a report that is generated and generally emailed at a specified interval.
     * 
     * @param report ScheduledPconfig that indicates the report, the parameters, the recipient and the schedule
     * @return String report id
     * @throws ReportingException
     */
    String saveScheduledReportConfig(ScheduledPconfig report) throws ReportingException;

    /**
     * Retrieves a scheduled report config given the scheduled report name/identifier.
     * 
     * @param reportId String scheduled report id/name
     * @return ScheduledPconfig
     */
    ScheduledPconfig getScheduledReport(String reportId);

    /**
     * Gets a list of the scheduled reports that have been saved for a specified report
     * 
     * @param pconfigId String id/name of the report, if null then all reports are returned
     * @return List of ScheduledPconfig reports, will not be null.
     */
    List<ScheduledPconfig> getScheduledReports(String pconfigId);

    /**
     * Generates the reports that are scheduled to be run now and then emails them to the specified person.
     * 
     * @throws ReportingException if any error occurs while finding or generated the report
     */
    void generateScheduledReports() throws ReportingException;

    /**
     * Deletes a specific scheduled report.
     * 
     * @param reportId String scheduled report name/id
     */
    void deleteScheduledReport(String reportId);

    /**
     * Deletes a specific generated report
     * 
     * @param reportId String name/identifier of the report
     */
    void deleteGeneratedReport(String reportId);

    /**
     * Reload all reports, generated reports and scheduled reports.
     */
    void refreshCache();

    /**
     * Delete generated reports older than the maxAge. Default maxAge is 7 days.
     */
    void deleteOldReports();

    /**
     * Determine the path to a generated report that exists on the filesystem of the server.
     * 
     * @param id String report name/id
     * @param type FileType indicating whether the report is PDF, CSV, etc
     * @return String a complete file path
     */
    public String getPath(String id, FileType type);
}
