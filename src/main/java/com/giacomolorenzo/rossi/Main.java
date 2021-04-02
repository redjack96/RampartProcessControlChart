package com.giacomolorenzo.rossi;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {

    private static final String PROJECT = "RAMPART";
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.lkogging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        Properties properties;
        GitManager gm;

        // Scrivo su file il git log di tutti i commit
        try(InputStream input = Main.class.getResourceAsStream("/config.properties")){
            properties = new Properties();
            if (input == null){
                logger.severe("Impossibile trovare il file di proprietà con la repository Git");
                throw new IOException();
            }

            properties.load(input);
            gm = new GitManager(properties);
            gm.writeCommit();
        } catch (GitAPIException io){
            io.printStackTrace();
        }

        // Ottengo gli ID dei ticket
        RetrieveTicketsID.doWork(PROJECT);
        // Scrivo su file i dettagli delle release
        GetReleaseInfo.writeReleaseInfo(PROJECT);
    }
}
