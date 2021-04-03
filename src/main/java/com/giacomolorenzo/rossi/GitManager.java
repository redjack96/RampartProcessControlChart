package com.giacomolorenzo.rossi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class GitManager implements VcsManager {
    private static final Logger logger = Logger.getLogger(GitManager.class.getName());
    private static final String GIT_DIRECTORY = "gitDir";
    private static final String FILE_DIR = "gitFile";
    private final Properties properties = PropertyManager.loadProperties();
    private Repository repository;
    private Git git;

    public GitManager() {
        initialize();
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
            e.printStackTrace();
        }
    }

    public void printBranches() {
        try {
            List<Ref> branches = git.branchList().call();
            for (Ref ref : branches) {
                logger.info(ref.getName());
            }

        } catch (GitAPIException gitAPIException) {
            gitAPIException.printStackTrace();
        }
    }

    public void writeCommit() {
        String message = "Writing commits of repo: " + repository;
        logger.info(message);
        File output = new File("git-log.txt");
        try(FileWriter fileWriter = new FileWriter(output.getAbsolutePath())){
            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit revCommit: commits) {
                fileWriter.append("DATE: ");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                fileWriter.append(dateFormat.format(revCommit.getAuthorIdent().getWhen()))
                        .append(" - ").append("COMMIT: ").append(revCommit.getFullMessage());
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
