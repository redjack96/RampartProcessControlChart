package com.giacomolorenzo.rossi.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MonthProcessControlChartData extends Writable{
    final String project;
    String versionName;
    long commitInMonth;
    int version;
    String yearMonthOfResolution;
    long fixedTicketWithCommit;
    double averageTicketPerMonth;
    double upperBound;
    double lowerBound;

    public MonthProcessControlChartData(Project project){
        this.project = project.getName();
    }

    @Override
    public String getFileNameSuffix() {
        return "-monthProcessControlChartData.csv";
    }
}
