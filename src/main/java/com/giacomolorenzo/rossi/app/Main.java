package com.giacomolorenzo.rossi.app;

import com.giacomolorenzo.rossi.control.GitManager;
import com.giacomolorenzo.rossi.control.ReleaseManager;
import com.giacomolorenzo.rossi.control.SvnManager;
import com.giacomolorenzo.rossi.control.TicketManager;
import com.giacomolorenzo.rossi.data.Project;

import java.util.logging.Logger;

import static com.giacomolorenzo.rossi.utils.Constants.RAMPART;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Attenzione: Il progetto RAMPART utilizza SVN. Non essendo riuscito a fare git svn checkout, per mancanza di memoria,
     * ho dovuto recuperare la storia di RAMPART da un Mirror presente su GitHub per utilizzare JGit.
     * @param args niente
     */
    public static void main(String[] args) {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        logger.info("Proprieta caricate con successo");

        Project rampart = new Project(RAMPART);

        SvnManager svn = new SvnManager();
        svn.writeCommitWithTickedID();


        // Ottengo gli ID dei ticket con risoluzione fixed. Crea RAMPART-TicketsID.csv con colonne:
        //                      TicketID,ResolutionDate
        //String fixedTicketsFileName = rampart.getTicketManager().findAllProjectFixedBugs();

        // Dopo un eventuale Git-Clone, recupero la storia dei commit e scrivo su un csv le seguenti colonne:
        //                      ticketID,commitDate,hasFixedTicket
        //GitManager gm = new GitManager(true, fixedTicketsFileName, rampart);
        //gm.printBranches();
        // vanno esclusi tutti i commit che non si riferiscono a un ticket risolto
        //gm.writeCommitWithTickedID();
        // poi scrivo tutti i mesi con almeno un commit
        //gm.writeMonthsWithNumberOfCommits();

        // Scrivo le release:
        //rampart.getReleaseManager().findAllReleases();
    }
}
