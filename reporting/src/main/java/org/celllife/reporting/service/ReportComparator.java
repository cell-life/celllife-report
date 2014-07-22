package org.celllife.reporting.service;

import java.util.Comparator;

import org.celllife.pconfig.model.FilledPconfig;

/**
 * Compares two generated reports and sorts them according to the date they were executed
 */
public class ReportComparator implements Comparator<FilledPconfig> {
    @Override
    public int compare(FilledPconfig o1, FilledPconfig o2) {
        String o1cmp = o1.getId();
        String o2cmp = o2.getId();
        
        o1cmp += o1.getDateFilled().toString();
        o2cmp += o2.getDateFilled().toString();
        
        int comapare = o1cmp.compareTo(o2cmp);
        return comapare;
    }
}
