package org.celllife.reporting.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.FileType;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.RepeatInterval;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.celllife.pconfig.util.PconfigUtils;
import org.celllife.reporting.ReportingException;
import org.celllife.reporting.service.ReportComparator;
import org.celllife.reporting.service.ReportLoader;
import org.celllife.reporting.service.ReportService;
import org.celllife.reporting.service.ScheduledReportComparator;
import org.celllife.reporting.util.JaxbUtil;
import org.celllife.utilities.mail.MailService;
import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class JasperReportServiceImpl implements ReportService {

    private static final String CONFIG_SUFFIX = "-config";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DataSource dataSource;

    private String sourceFolder;

    private String generatedFolder;

    private String scheduledFolder;

    private File generatedFolderFile;

    private File scheduledFolderFile;

    private JaxbUtil jaxbUtil;

    private ReportLoader loader;

    private MailService mailService;

    private int maxAge = 7;

    // Cache of reports
    private final Map<String, Pconfig> reportMap = new HashMap<String, Pconfig>();

    // Cache of generated reports
    private final Map<String, FilledPconfig> generatedReportMap = new HashMap<String, FilledPconfig>();

    // Cache of scheduled reports
    private final Map<String, ScheduledPconfig> scheduledReportMap = new HashMap<String, ScheduledPconfig>();

    @Override
    public List<Pconfig> getReports() {
        List<Pconfig> list = new ArrayList<Pconfig>();
        list.addAll(reportMap.values());
        return list;
    }

    @Override
    public Pconfig getReportByName(String name) {
        return reportMap.get(name);
    }

    @Override
    public List<Pconfig> getReportsByProperty(final String propertyName, final String value) {
        @SuppressWarnings("unchecked")
        Collection<Pconfig> reports = CollectionUtils.select(reportMap.values(), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Pconfig report = (Pconfig) object;
                if (!report.hasProperty(propertyName)) {
                    return false;
                } else if (value != null) {
                    return report.getProperty(propertyName).equals(value);
                }

                return true;
            }
        });
        List<Pconfig> list = new ArrayList<Pconfig>();
        list.addAll(reports);
        return list;
    }

    @Override
    public String generateReport(Pconfig report) throws ReportingException {
        return generateReport(report, FileType.PDF);
    }

    @Override
    public String generateReport(Pconfig report, FileType type) throws ReportingException {
        File reportFile = loader.getReportFile(report, sourceFolder);
        if (reportFile == null) {
            log.error("Unable to find report file: {}", report.getResource());
            throw new ReportingException("Unable to find report file: " + report.getResource());
        }

        String id = getReportId();
        String exportPath = getPath(id, type);

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(reportFile);
        } catch (FileNotFoundException e) {
            log.error("Could not find report file: " + reportFile.getAbsolutePath(), e);
            throw new ReportingException(e);
        }

        Connection conn = null;
        if (dataSource != null) {
            try {
                conn = dataSource.getConnection();
            } catch (SQLException e) {
                log.error("Error getting report connection.", e);
            }
        } else {
            log.warn("Datasource is null. Will attempt to generate report without connection");
        }

        File reportFolder = reportFile.getParentFile();

        Map<String, Object> map = getParameterMap(report);
        map.put("SUBREPORT_DIR", reportFolder.getAbsolutePath() + File.separator);

        if (log.isDebugEnabled()) {
            log.debug("Report parameter map: " + map);
        }

        JasperPrint jasperPrint = null;
        try {
            if (conn == null) {
                jasperPrint = JasperFillManager.fillReport(fileInputStream, map, new JREmptyDataSource(1));
            } else {
                jasperPrint = JasperFillManager.fillReport(fileInputStream, map, conn);
            }
            File absolutePathFile = new File(exportPath);
            String absolutePath = absolutePathFile.getAbsolutePath();

            switch (type) {
            case HTML:
                JasperExportManager.exportReportToHtmlFile(jasperPrint, absolutePath);
                break;
            case PDF:
                JasperExportManager.exportReportToPdfFile(jasperPrint, absolutePath);
                break;
            case XML:
                JasperExportManager.exportReportToXmlFile(jasperPrint, absolutePath, false);
                break;
            case CSV:
                JRCsvExporter exporter = new JRCsvExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE, absolutePathFile);
                exporter.exportReport();
                break;
            }

            FilledPconfig filledPconfig = new FilledPconfig();
            filledPconfig.setPconfig(report);
            filledPconfig.setResourceType(type.name());
            filledPconfig.setResourcePath(absolutePath);
            filledPconfig.setId(id);
            filledPconfig.setDateFilled(new Date());
            writeReportConfig(filledPconfig);
        } catch (Exception e) {
            log.error("Error generating report", e);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Exception closing report connection.", e);
            }
        }

        return id;
    }

    @Override
    public ScheduledPconfig getScheduledReport(String reportID) {
        return scheduledReportMap.get(reportID);
    }

    private Map<String, Object> getParameterMap(Pconfig report) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (report.getParameters() != null) {
            for (Parameter<?> param : report.getParameters()) {
                if (param instanceof LabelParameter) {
                    continue;
                }
    
                Object value = param.getValue();
                if (value == null) {
                    value = param.getDefaultValue();
                }
    
                if (value != null && param instanceof EntityParameter) {
                    EntityParameter eParam = (EntityParameter) param;
                    String valueType = eParam.getValueType();
                    if (valueType != null && !valueType.isEmpty()) {
                        value = PconfigUtils.convertValue((String) value, valueType);
                    }
                }
    
                if (value != null) {
                    map.put(param.getName(), value);
                }
            }
        }
        return map;
    }

    public String getPath(String id, FileType type) {
        return generatedFolder + File.separator + id + type.getExtension();
    }

    private String getScheduledReportPath(String id, FileType type) {
        return scheduledFolder + File.separator + id + type.getExtension();
    }

    private void writeReportConfig(FilledPconfig report) throws FileNotFoundException, ReportingException {
        String path = getPath(report.getId() + CONFIG_SUFFIX, FileType.XML);
        File file = new File(path);
        jaxbUtil.write(report, new FileOutputStream(file));
        generatedReportMap.put(report.getId(), report);
    }

    @Override
    public String saveScheduledReportConfig(ScheduledPconfig report) throws ReportingException {
        if (report.getPconfig() == null) {
            throw new ReportingException("Can not save report schedule without the report configuration.");
        }

        if (report.getId() != null) {
            deleteScheduledReport(report.getId());
        } else {
            String id = getReportId();
            report.setId(id);
        }
        String path = getScheduledReportPath(report.getId() + CONFIG_SUFFIX, FileType.XML);
        try {
            jaxbUtil.write(report, new FileOutputStream(new File(path)));
        } catch (FileNotFoundException e) {
            throw new ReportingException(e);
        }
        scheduledReportMap.put(report.getId(), report);
        return report.getId();
    }

    @Override
    public void deleteScheduledReport(final String id) {
        scheduledReportMap.remove(id);

        File[] list = scheduledFolderFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(id);
            }
        });

        deleteFiles(list);
    }

    @Override
    public List<ScheduledPconfig> getScheduledReports(final String pconfigId) {
        Collection<ScheduledPconfig> reports = scheduledReportMap.values();

        if (pconfigId != null && !pconfigId.isEmpty()) {
            @SuppressWarnings("unchecked")
            Collection<ScheduledPconfig> selection = CollectionUtils.select(reports, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ((ScheduledPconfig) object).getPconfig().getId().equals(pconfigId);
                }
            });

            List<ScheduledPconfig> list = new ArrayList<ScheduledPconfig>(selection);
            return list;
        } else {
            List<ScheduledPconfig> list = new ArrayList<ScheduledPconfig>(reports);
            Collections.sort(list, new ScheduledReportComparator());
            return list;
        }
    }

    @Override
    @Scheduled(cron = "${report.scheduled.cron}")
    public void generateScheduledReports() throws ReportingException {
        log.info("Running scheduled reports now ...");

        // refresh the scheduled report cache
        refreshScheduledReportCache();

        // find out which scheduled reports need to be executed
        Date beginningOfToday = getAfterMidnightLastNight();
        Date endOfToday = getBeforeMidnightTonight();
        for (ScheduledPconfig spconfig : scheduledReportMap.values()) {
            // check the start and end date for the scheduled report
            Date startDate = spconfig.getStartDate();
            Date endDate = spconfig.getEndDate();
            if (endOfToday.before(startDate) || beginningOfToday.after(endDate)) {
                if (log.isDebugEnabled()) {
                    log.debug("Not running scheduled report " + spconfig.getId()
                            + " because it is scheduled to run between " + startDate + " and " + endDate
                            + " and it is now " + new Date());
                }
                continue;
            }
            // check the current interval
            RepeatInterval repeatInterval = spconfig.getRepeatInterval();
            int intervalCount = spconfig.getIntervalCount();
            if (intervalCount != 1) {
                int currentInterval = getCurrentInterval(endOfToday, startDate, repeatInterval);
                if (currentInterval % intervalCount != 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Not running scheduled report " + spconfig.getId()
                                + " because it is scheduled to run every " + intervalCount
                                + " interval and it is now the " + currentInterval + " interval.");
                    }
                    continue;
                }
            }
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar todayCal = Calendar.getInstance();
            if (repeatInterval == RepeatInterval.Daily
                    || (repeatInterval == RepeatInterval.Weekly && startCal.get(Calendar.DAY_OF_WEEK) == todayCal
                            .get(Calendar.DAY_OF_WEEK))
                    || (repeatInterval == RepeatInterval.Monthly && startCal.get(Calendar.DAY_OF_MONTH) == todayCal
                            .get(Calendar.DAY_OF_MONTH))) {

                // Finally, generate the report
                String reportId = null;
                if (spconfig.getFileType() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Running scheduled PDF report " + spconfig.getId());
                    }
                    reportId = generateReport(spconfig.getPconfig(), FileType.PDF);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Running scheduled " + spconfig.getFileType() + "report " + spconfig.getId());
                    }
                    reportId = generateReport(spconfig.getPconfig(), spconfig.getFileType());
                }

                // And email!
                File report = getGeneratedReportFile(reportId);
                String emailAddress = spconfig.getScheduledFor();
                if (mailService != null && emailAddress != null) {
                    log.info("Emailing scheduled report " + report + " to " + emailAddress);
                    mailService.sendEmail(emailAddress, spconfig.getPconfig().getLabel(),
                            "Please see attached report.", report);
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Not running scheduled report " + spconfig.getId()
                            + " because it is scheduled to run every " + intervalCount + " from " + startDate
                            + " and it is now " + new Date());
                }
            }
        }
    }

    Date getAfterMidnightLastNight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return cal.getTime();
    }

    Date getBeforeMidnightTonight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    int getCurrentInterval(Date now, Date startDate, RepeatInterval repeatInterval) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int currentInterval = 1;
        while (cal.getTime().before(now)) {
            if (repeatInterval == RepeatInterval.Daily) {
                cal.roll(Calendar.DAY_OF_YEAR, true);
            } else if (repeatInterval == RepeatInterval.Weekly) {
                cal.roll(Calendar.WEEK_OF_YEAR, true);
            } else if (repeatInterval == RepeatInterval.Monthly) {
                cal.roll(Calendar.MONTH, true);
            }
            currentInterval++;
        }
        return currentInterval;
    }

    private String getReportId() {
        return String.valueOf(new Date().getTime());
    }

    @Override
    public List<FilledPconfig> getGeneratedReports(final String id) {
        Collection<FilledPconfig> reports = generatedReportMap.values();

        if (id != null && !id.isEmpty()) {
            @SuppressWarnings("unchecked")
            Collection<FilledPconfig> selection = CollectionUtils.select(reports, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ((FilledPconfig) object).getPconfig().getId().equals(id);
                }
            });

            List<FilledPconfig> list = new ArrayList<FilledPconfig>(selection);
            return list;
        } else {
            List<FilledPconfig> list = new ArrayList<FilledPconfig>(reports);
            Collections.sort(list, new ReportComparator());
            return list;
        }
    }

    @Override
    public FilledPconfig getGeneratedReport(String id) {
        return generatedReportMap.get(id);
    }

    @Override
    public File getGeneratedReportFile(String id) {
        FilledPconfig report = getGeneratedReport(id);
        if (report != null) {
            File file = new File(report.getResourcePath());
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private void deleteGeneratedReportFromDisk(final String generatedId) {
        File[] list = generatedFolderFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(generatedId);
            }
        });

        deleteFiles(list);
    }

    private void deleteFiles(File[] list) {
        for (File f : list) {
            if (!f.exists()) {
                log.warn("Unable to delete report file. " + "File does not exist: {}", f.getAbsolutePath());
            }

            if (!f.canWrite()) {
                log.warn("Unable to delete report file. " + "File write protected: {}", f.getAbsolutePath());
            }

            if (f.isDirectory()) {
                log.warn("Unable to delete report file. " + "File is a directory: {}", f.getAbsolutePath());
            }

            boolean success = f.delete();

            if (!success) {
                log.warn("Unable to delete report file: {}", f.getAbsolutePath());
            }
        }
    }

    @Override
    public void deleteGeneratedReport(final String generatedId) {
        deleteGeneratedReportFromDisk(generatedId);
        generatedReportMap.remove(generatedId);
    }

    public void buildService() throws ReportingException {
        try {
            jaxbUtil = new JaxbUtil(Pconfig.class);
        } catch (JAXBException e) {
            throw new ReportingException("Unable to create JaxbUtil instance");
        }

        initGeneratedFolder();
        initScheduledFolder();
        refreshCache();
    }

    @Override
    public void refreshCache() {
        reportMap.clear();
        generatedReportMap.clear();
        scheduledReportMap.clear();

        Collection<Pconfig> reports = loader.loadReports(jaxbUtil, sourceFolder, null);
        for (Pconfig report : reports) {
            reportMap.put(report.getId(), report);
        }

        Collection<FilledPconfig> generatedReports = loader.loadReports(jaxbUtil, generatedFolder, CONFIG_SUFFIX);
        for (FilledPconfig report : generatedReports) {
            String id = report.getId();
            if (id != null && !id.isEmpty()) {
                generatedReportMap.put(id, report);
            } else {
                log.warn("Warning: Generated report loaded without id property: " + report.getId());
            }
        }

        refreshScheduledReportCache();
    }

    private void refreshScheduledReportCache() {
        Collection<ScheduledPconfig> scheduledReports = loader.loadReports(jaxbUtil, scheduledFolder, CONFIG_SUFFIX);
        for (ScheduledPconfig report : scheduledReports) {
            String id = report.getId();
            if (id != null && !id.isEmpty()) {
                log.debug("Found report: " + report.getId() + " for report " + report.getPconfig().getId());
                scheduledReportMap.put(id, report);
            } else {
                log.warn("Warning: Scheduled report loaded without id property: " + report.getId());
            }
        }
    }

    @Override
    @Scheduled(cron = "${report.delete.cron}")
    public void deleteOldReports() {
        if (log.isDebugEnabled()) {
            log.debug("Deleting reports older than " + maxAge + " days old from " + generatedFolder);
        }
        Collection<FilledPconfig> reports = generatedReportMap.values();
        Iterator<FilledPconfig> iterator = reports.iterator();
        while (iterator.hasNext()) {
            FilledPconfig report = iterator.next();
            Date dateGenerated = report.getDateFilled();
            Days days = Days.daysBetween(new DateMidnight(dateGenerated), new DateMidnight(new Date()));
            if (days.getDays() > maxAge) {
                deleteGeneratedReportFromDisk(report.getId());
                iterator.remove();
            }
        }
    }

    private void initGeneratedFolder() throws ReportingException {
        generatedFolderFile = new File(generatedFolder);
        if (!generatedFolderFile.exists()) {
            generatedFolderFile.mkdirs();
        }

        if (!generatedFolderFile.isDirectory()) {
            log.warn("Directory for generated reports is not a directory: {}", generatedFolderFile.getAbsolutePath());
        }
    }

    private void initScheduledFolder() throws ReportingException {
        scheduledFolderFile = new File(scheduledFolder);
        if (!scheduledFolderFile.exists()) {
            scheduledFolderFile.mkdirs();
        }

        if (!scheduledFolderFile.isDirectory()) {
            log.warn("Directory for generated reports is not a directory: {}", scheduledFolderFile.getAbsolutePath());
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setReportLoader(ReportLoader loader) {
        this.loader = loader;
    }

    public void setGeneratedReportFolder(String folder) {
        this.generatedFolder = folder;
    }

    public void setScheduledReportFolder(String folder) {
        this.scheduledFolder = folder;
    }

    public void setSourceReportFolder(String folder) {
        this.sourceFolder = folder;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setMaxAge(int days) {
        this.maxAge = days;
    }

}
