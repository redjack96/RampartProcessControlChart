package com.giacomolorenzo.rossi.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * Un commit git o una revisione svn.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Commit extends Writable{
    String project;
    String vcsName;
    Date commitDate;
    String commitHash;
    List<String> tickets;
    boolean hasFixedTicket;

    public Commit(Project project){
        this.project = project.getName();
    }

    public Commit(Project project, String vcsName, Date commitDate, String commitHash, List<String> tickets, boolean hasFixedTicket) {
        this.project = project.getName();
        this.vcsName = vcsName;
        this.commitDate = commitDate;
        this.commitHash = commitHash;
        this.tickets = tickets;
        this.hasFixedTicket = hasFixedTicket;
    }

    @Override
    public String getFileNameSuffix() {
        return "-commits.csv";
    }
}
