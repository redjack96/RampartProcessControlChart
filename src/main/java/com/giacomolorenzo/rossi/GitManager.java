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
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class GitManager {
    private static final Logger logger = Logger.getLogger(GitManager.class.getName());
    private static final String GIT_DIRECTORY = "gitDir";
    private static final String FILE_DIR = "gitFile";
    private final Repository repository;
    private final Git git;

    public GitManager (Properties properties) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder.setGitDir(new File(properties.getProperty(GIT_DIRECTORY)))
                .readEnvironment()
                .findGitDir()
                .build();
        this.git = Git.open(new File(properties.getProperty(FILE_DIR)));

    }

    public void writeCommit() throws GitAPIException {
        String repoName = String.valueOf(repository);
        logger.info(repoName);
        List<Ref> branches = git.branchList().call();
        for (Ref ref : branches) {
            logger.info(ref.getName());
        }

        try(FileWriter fileWriter = new FileWriter(new File(Main.class.getResource("/git-log.txt").toURI()))){
            Iterable<RevCommit> commits = git.log().all().call();
            for(RevCommit revCommit: commits){
                fileWriter.append("COMMIT: ").append(revCommit.getFullMessage())
                        .append("\n").append(" - DATE: ");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                fileWriter.append(dateFormat.format(revCommit.getAuthorIdent().getWhen().toString()))
                        .append("\n");
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
