package com.giacomolorenzo.rossi.data;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Un commit git o una revisione svn.
 */
@Data
public class Commit {
    private String vcsName;
    private Date commitDate;
    private String commitHash;
    private List<String> tickets;
    private boolean hasFixedTicket;

    public Commit(String vcsName, Date commitDate, String commitHash, List<String> tickets, boolean hasFixedTicket) {
        this.vcsName = vcsName;
        this.commitDate = commitDate;
        this.commitHash = commitHash;
        this.tickets = tickets;
        this.hasFixedTicket = hasFixedTicket;
    }
}
