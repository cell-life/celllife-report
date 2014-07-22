package org.celllife.reporting.service;

import java.util.Comparator;

import org.celllife.pconfig.model.ScheduledPconfig;

/**
 * Compares two scheduled reports and sorts them according to the scheduled start date.
 */
public class ScheduledReportComparator implements Comparator<ScheduledPconfig> {
    @Override
    public int compare(ScheduledPconfig o1, ScheduledPconfig o2) {
        int comapare = o1.getStartDate().compareTo(o2.getStartDate());
        return comapare;
    }
}
