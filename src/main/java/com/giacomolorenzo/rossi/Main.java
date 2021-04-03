package com.giacomolorenzo.rossi;

import java.util.Properties;
import java.util.logging.Logger;

public class Main {

    private static final String PROJECT = "RAMPART";
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.lkogging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        Properties properties = PropertyManager.loadProperties();
        logger.info("Proprieta caricate con successo");
        // Scrivo su file il git log di tutti i commit
        GitManager gm = new GitManager(properties);
        gm.printBranches();
        gm.writeCommit();
        // Ottengo gli ID dei ticket
        RetrieveTicketsID.doWork(PROJECT);
        // Scrivo su file i dettagli delle release
        GetReleaseInfo.writeReleaseInfo(PROJECT);
    }
}
