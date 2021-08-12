package com.giacomolorenzo.rossi.control;

import com.giacomolorenzo.rossi.data.Project;
import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitManager implements VcsManager {
    private static final Logger logger = Logger.getLogger(GitManager.class.getName());
    private static final String GIT_DIRECTORY = "gitDir";
    private static final String FILE_DIR = "gitFile";
    private final Properties properties = PropertyManager.loadProperties();
    private final String fixedTicketsFileName;
    private Repository repository;
    private Git git;

    public GitManager(boolean online, String fixedTicketsFileName, Project project) {
        if (online) {
            initializeRemote();
        }
        initialize();
        this.fixedTicketsFileName = fixedTicketsFileName;
    }

    private void initialize() {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            repository = builder.setGitDir(new File(properties.getProperty(GIT_DIRECTORY)))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            this.git = Git.open(new File(properties.getProperty(FILE_DIR)));
        } catch (IOException e) {
            logger.severe("Impossibile inizilizzare la repo locale Git");
        }
    }

    private void initializeRemote() {
        try {
            File gitDirectory = new File(properties.getProperty(GIT_DIRECTORY));
            if (!gitDirectory.exists()) {
                boolean mkdir = gitDirectory.mkdir();
                if (!mkdir) throw new IOException();
                logger.info("Sto clonando il repository Git per il progetto RAMPART!");
                Git.cloneRepository()
                        .setURI("https://github.com/apache/rampart.git")
                        .setDirectory(gitDirectory)
                        .call();
                logger.info("Repository Git clonato con successo!");
            }
            this.git = Git.open(new File(properties.getProperty(FILE_DIR)));
        } catch (IOException | GitAPIException e) {
            logger.severe("Impossibile clonare la repo Git");
        }
    }

    public void printBranches() {
        try {
            List<Ref> branches = git.branchList().call();
            for (Ref ref : branches) {
                logger.info(ref.getName());
            }

        } catch (GitAPIException gitAPIException) {
            logger.severe("Impossibile stampare i branch Git");
        }
    }

    public void writeCommitWithTickedID() {
        String projectName = properties.getProperty("project");
        String messageToLog = "Writing commits of " + repository;
        logger.info(messageToLog);
        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(projectName + "-git-commits.csv")),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeNext(new String[]{"ticketID","commitDate","hasFixedTicket"});
            Iterable<RevCommit> logEntries = git.log().all().call();
            for (RevCommit logEntry : logEntries) {
                List<String> ticketIds = new ArrayList<>();
                Date date = logEntry.getCommitterIdent().getWhen();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateFormatted = simpleDateFormat.format(date); // da stampare
                String message = logEntry.getFullMessage();
                // Cerco gli Id dei ticket nel messaggio di commit
                Pattern pattern = Pattern.compile("RAMPART-[0-9]*");
                Matcher matcher = pattern.matcher(message);

                while (matcher.find()) {
                    ticketIds.add(matcher.group(0)); // ognuno di questi e' da stampare
                }
                for (String ticketId : ticketIds) {

                    String thereIs = hasFixedTicket(ticketId);
                    String[] row = new String[]{ticketId, dateFormatted, thereIs};
                    csvWriter.writeNext(row);
                }
            }
        } catch (IOException | GitAPIException e) {
            logger.severe("Impossibile scrivere su file i commit Git");
        }
    }

    /**
     * Controlla se il ticketId presente nel commit si riferisce a un fixed ticket
     *
     * @param ticketId presente nel commit
     * @return true se si riferisce a un fixed ticket nel file fixedTicketsFileName.
     */
    private String hasFixedTicket(String ticketId) {
        boolean hasFixedTicket = false;
        try (FileReader fileReader = new FileReader(fixedTicketsFileName)) {
            StringWriter s = new StringWriter();
            long result = fileReader.transferTo(s);
            String fileContents = s.toString();
            if (result == 0) return Boolean.toString(false);
            else if (fileContents.contains(ticketId) || fileContents.contains(ticketId.toLowerCase(Locale.ROOT))
                    || fileContents.contains(ticketId.substring(0,1).toUpperCase(Locale.ROOT)+ticketId.substring(1))) {
                hasFixedTicket = true;
            }
        } catch (IOException e) {
            logger.severe("Impossibile verificare se il ticket "+ticketId+" contenuto nel messagio di Git commit e' un fixed ticket in Jira");
        }
        return Boolean.toString(hasFixedTicket);
    }

    public void writeMonthsWithNumberOfCommits() {
        String projectName = properties.getProperty("project");
        String messageToLog = "Writing commits of " + repository;
        logger.info(messageToLog);
        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(projectName + "-commit-months.csv")),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            Iterable<RevCommit> logEntries = git.log().all().call();
            Map<String, Integer> mesiConNumeroCommit = new HashMap<>();
            for (RevCommit logEntry : logEntries) {
                Date date = logEntry.getCommitterIdent().getWhen();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
                String dateFormatted = simpleDateFormat.format(date); // da stampare
                if(!mesiConNumeroCommit.containsKey(dateFormatted)){
                    mesiConNumeroCommit.put(dateFormatted, 1); // se il mese non era presente, lo aggiungiamo.
                } else {
                    int numeroCommit = mesiConNumeroCommit.get(dateFormatted);
                    numeroCommit++;
                    mesiConNumeroCommit.put(dateFormatted, numeroCommit);
                }
            }
            // scrivo l'header del csv
            csvWriter.writeNext(new String[]{"commitMonth","commits"});

            List<String[]> listaDeiMesiConNumeroCommit = mesiConNumeroCommit.entrySet()
                    .stream()
                    .map(stringIntegerEntry -> new String[]{stringIntegerEntry.getKey(), stringIntegerEntry.getValue().toString()})
                    .collect(Collectors.toList());

            // ora scrivo tutte le righe nel commit-months.csv
            csvWriter.writeAll(listaDeiMesiConNumeroCommit);
        } catch (IOException | GitAPIException e) {
            logger.severe("Impossibile scrivere su file i mesi con il numero di git commit per ciascuno");
        }
    }
}
