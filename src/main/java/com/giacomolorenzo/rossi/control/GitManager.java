package com.giacomolorenzo.rossi.control;

import com.giacomolorenzo.rossi.data.Commit;
import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.utils.VcsUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class GitManager {
    private static final Logger logger = Logger.getLogger(GitManager.class.getName());
    private static final String GIT_DIRECTORY = "gitDir";
    private static final String FILE_DIR = "gitFile";
    private final Properties properties = PropertyManager.loadProperties();
    private final Project project;
    private Git git;

    public GitManager(boolean online, Project project) {
        if (online) {
            initializeRemote();
        }
        initialize();
        this.project = project;
    }

    private void initialize() {
        try {
            var builder = new FileRepositoryBuilder();
            builder.setGitDir(new File(properties.getProperty(GIT_DIRECTORY)))
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

    private List<RevCommit> getRevCommits() {
        List<RevCommit> revCommits = new ArrayList<>();
        try {
            Iterable<RevCommit> revCommitIterable = git.log().all().call();

            for (RevCommit revCommit : revCommitIterable) {
                revCommits.add(revCommit);
            }
        } catch (IOException | GitAPIException ex) {
            Logger.getLogger(GitManager.class.getSimpleName()).warning(ex.getMessage());
        }
        revCommits.sort(Comparator.comparingLong(value -> value.getAuthorIdent().getWhen().getTime()));
        return revCommits;
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

    public List<Commit> findAllCommits() {
        List<Commit> commits = new ArrayList<>();
        List<RevCommit> revCommits = getRevCommits();
        for (RevCommit revCommit : revCommits) {
            var messaggio = revCommit.getFullMessage();
            List<String> ticketList = VcsUtils.getTicketsFromCommitMessage(messaggio, project);
            boolean hasFixedTicket = VcsUtils.hasFixedTicket(ticketList, project);
            var c = new Commit(project, "git", revCommit.getAuthorIdent().getWhen(), revCommit.getName(), ticketList, hasFixedTicket);
            commits.add(c);
        }
        return commits;
    }

}
