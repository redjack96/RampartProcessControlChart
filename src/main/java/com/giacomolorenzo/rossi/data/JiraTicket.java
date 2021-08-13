package com.giacomolorenzo.rossi.data;

import com.giacomolorenzo.rossi.utils.DateUtils;
import lombok.Data;
import lombok.NonNull;

import java.time.YearMonth;
import java.util.Date;
import java.util.Objects;

@Data
public class JiraTicket extends Writable{
    private static final String TICKET_FILE_SUFFIX = "-TicketInfo.csv";
    String project;
    String ticketID;
    String type;
    String resolution;
    Date resolutionDate; // necessario per capire a quale release appartiene.

    public JiraTicket(Project project){
        this.project = project.getName();
    }

    public JiraTicket(@NonNull String ticketID, Project project) {
        this.project = project.getName();
        this.ticketID = ticketID;
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

    public boolean isInMonth(YearMonth ym){
        return DateUtils.isInMonth(this.resolutionDate, ym);
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

    public String getProjectName() {
        return this.getTicketID().split("-")[0];
    }
}
