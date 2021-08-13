package com.giacomolorenzo.rossi.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProcessControlChartData extends Writable{
    final String project;
    String versionName;
    int commitInRelease;
    int version;
    String releaseDate;
    long fixedTicketWithCommit;
    double averageTicketPerRelease;
    double upperBound;
    double lowerBound;

    public ProcessControlChartData(Project project){
        this.project = project.getName();
    }

    @Override
    public String getFileNameSuffix() {
        return "-processControlChartData.csv";
    }
}
