package com.giacomolorenzo.rossi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String PROJECT = "RAMPART";
    private static final Logger l = MyLogger.get();

    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
                // [timestamp] package.className methodName() Severity: the message & the stacktrace\n
        l.log(Level.INFO, "Ciao!");
        l.log(Level.WARNING, "Benvenuti nel progetto " + PROJECT);
        RetrieveTicketsID.doWork(PROJECT);
    }

}
