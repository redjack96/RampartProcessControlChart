package com.giacomolorenzo.rossi;

import java.util.logging.Logger;

public class Main {

    private static final String PROJECT = PropertyManager.loadProperties().getProperty("project");
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.lkogging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        logger.info("Proprieta caricate con successo");
        // Il progetto RAMPART utilizza SVN. Non essendo riuscito a fare il checkout, provo il codice per leggere da una repo git con un'altra repository
        GitManager gm = new GitManager();
        gm.printBranches();
        gm.writeCommit();

        // Mi collego alla repository di RAMPART per poter ottenere il log della storia.
        SvnManager sm = new SvnManager();
        sm.printBranches();
        sm.writeCommit();

        // Ottengo gli ID dei ticket
        RetrieveTicketsID.doWork(PROJECT);
        // Scrivo su file i dettagli delle release
        GetReleaseInfo.writeReleaseInfo(PROJECT);
    }
}
