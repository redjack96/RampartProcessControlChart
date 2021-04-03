package com.giacomolorenzo.rossi;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Attenzione: Il progetto RAMPART utilizza SVN. Non essendo riuscito a fare git svn checkout, per mancanza di memoria,
     * ho dovuto recuperare la storia di RAMPART da un Mirror presente su GitHub per utilizzare JGit.
     * @param args niente
     */
    public static void main(String[] args) {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.lkogging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        logger.info("Proprieta caricate con successo");


        // Ottengo gli ID dei ticket con risoluzione fixed. Crea RAMPART-TicketsID.csv con colonne:
        //                      TicketID,ResolutionDate
        String fixedTicketsFileName = RetrieveTicketsID.writeFixedIssues();

        // Dopo un eventuale Git-Clone, recupero la storia dei commit e scrivo su un csv le seguenti colonne:
        //                      ticketID,commitDate,hasFixedTicket
        GitManager gm = new GitManager(true, fixedTicketsFileName);
        gm.printBranches();
        // vanno esclusi tutti i commit che non si riferiscono a un ticket risolto
        gm.writeCommitWithTickedID();
        // poi scrivo tutti i mesi con almeno un commit
        gm.writeMonthsWithNumberOfCommits();
    }
}
