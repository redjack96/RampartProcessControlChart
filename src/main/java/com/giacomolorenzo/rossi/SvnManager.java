package com.giacomolorenzo.rossi;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvnManager implements VcsManager {

    private final Properties properties = PropertyManager.loadProperties();
    private static final Logger logger = Logger.getLogger(SvnManager.class.getName());
    private SVNRepository repository;

    public SvnManager() {
        initialize();
    }

    /**
     * Inizializza la repository del progetto;
     */
    private void initialize() {
        String url = properties.getProperty("svnUrl");

        try {
            SVNURL svnUrl = SVNURL.parseURIEncoded(url);
            repository = SVNRepositoryFactory.create(svnUrl);

        } catch (SVNException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printBranches() {
        logger.info("SVN non ha il concetto di branch");
    }

    @Override
    public void writeCommitWithTickedID() {
        String projectName = properties.getProperty("project");
        try (FileWriter fileWriter = new FileWriter(projectName + "-svn-commits.csv")) {

            var logEntries = repository.log(new String[]{""}, null, 0, -1, true, true);
            for (Object object : logEntries) {
                SVNLogEntry logEntry = (SVNLogEntry) object;
                Date date = logEntry.getDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                String dateFormatted = simpleDateFormat.format(date);
                String message = logEntry.getMessage();
                if (message.contains(properties.getProperty("project") + "-")) {
                    List<String> ticketIds = new ArrayList<>();
                    Pattern pattern = Pattern.compile(projectName + "-[0-9]*");
                    Matcher matcher = pattern.matcher(message);

                    Long revision = logEntry.getRevision();
                    while (matcher.find()) {
                        ticketIds.add(matcher.group(0));
                    }
                    for (String ticketId : ticketIds) {
                        String commit = revision + "," + ticketId + "," + dateFormatted;
                        fileWriter.append(commit).append("\n");
                    }
                }
                String messageToLog = logEntry.getRevision() + "," + simpleDateFormat.format(logEntry.getDate()) + "," + message;
                logger.info(messageToLog);
            }
        } catch (SVNException | IOException e) {
            e.printStackTrace();
        }
    }
}
