package com.giacomolorenzo.rossi.utils;

import com.giacomolorenzo.rossi.data.JiraTicket;
import com.giacomolorenzo.rossi.data.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VcsUtils {

    private VcsUtils() {
    }

    public static List<String> getTicketsFromCommitMessage(String commitMessage, Project project) {
        List<String> ticketIds = new ArrayList<>();
        // Cerco gli Id dei ticket nel messaggio di commit
        var pattern = Pattern.compile(project + "-[0-9]*"); // esempi: AVRO-### | BOOKKEEPER-###
        var matcher = pattern.matcher(commitMessage);

        while (matcher.find()) {
            ticketIds.add(matcher.group(0)); // ogni ticket riferito va aggiunto alla lista
        }
        return ticketIds;
    }


    /**
     * Controlla se almeno un ticketId presente nel commit/revisione si riferisce a un fixed ticket
     *
     * @param ticketsInMessage i ticket presenti nel messaggio di commit
     * @return true se si riferisce a un fixed ticket nel file fixedTicketsFileName.
     */
    public static boolean hasFixedTicket(List<String> ticketsInMessage, Project project) {
        return project.getAllFixedTickets().stream()
                .map(JiraTicket::getTicketID) // lista di ticketId
                .anyMatch(ticketsInMessage::contains); // true se tra i ticket del messaggio è contenuto almeno un ticket fixed.
    }
}
