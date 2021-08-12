package com.giacomolorenzo.rossi.data;

import com.giacomolorenzo.rossi.utils.DateUtils;
import com.giacomolorenzo.rossi.utils.FileUtils;
import com.giacomolorenzo.rossi.utils.ReleaseUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class JiraTicket extends Writable implements Readable{
    private static final String TICKET_FILE_SUFFIX = "-TicketInfo.csv";
    Project project;
    String ticketID;
    String type;
    String resolution;
    Date resolutionDate; // necessario per capire a quale release appartiene.

    public JiraTicket(Project project){
        this.project = project;
    }

    public JiraTicket(@NonNull String ticketID, Project project) {
        this.ticketID = ticketID;
        this.project = project;
    }

    @Override
    public String toString() {
        return "JiraTicket{" +
                " project='" + project + '\'' +
                ", ticketID='" + ticketID + '\'' +
                ", type='" + type + '\'' +
                ", resolution='" + resolution + '\'' +
                '}';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JiraTicket that = (JiraTicket) o;
        return ticketID.equals(that.ticketID);
    }

    public String getFileNameSuffix() {
        return TICKET_FILE_SUFFIX;
    }

    public int hashCode() {
        return Objects.hash(ticketID);
    }

    public JiraTicket loadFromCSV(String[] riga) {
        var jiraTicket = new JiraTicket(riga[1], project);
        jiraTicket.setType(riga[2]);
        jiraTicket.setResolution(riga[3]);
        return jiraTicket;
    }

    public String getProjectName() {
        return this.getTicketID().split("-")[0];
    }
}
