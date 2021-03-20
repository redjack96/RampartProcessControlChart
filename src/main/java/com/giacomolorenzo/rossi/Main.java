package com.giacomolorenzo.rossi;

import java.io.IOException;

public class Main {

    private static final String PROJECT = "RAMPART";

    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
                // [timestamp] package.className methodName() Severity: the message & the stacktrace\n
        RetrieveTicketsID.doWork(PROJECT);
    }

}
