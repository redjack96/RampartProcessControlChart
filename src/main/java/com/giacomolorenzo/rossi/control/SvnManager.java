package com.giacomolorenzo.rossi.control;

import com.giacomolorenzo.rossi.data.Commit;
import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.utils.VcsUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class SvnManager {

    private final Properties properties = PropertyManager.loadProperties();
    private static final Logger logger = Logger.getLogger(SvnManager.class.getName());
    private final Project project;
    private SVNRepository repository;

    public SvnManager(Project project) {
        this.project = project;
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
            logger.severe("Impossibile inizializzare la repo SVN");
        }
    }

    public List<Commit> findAllRevisions() {
        List<Commit> revisions = new ArrayList<>();
        try {
            var logEntries = repository.log(new String[]{""}, null, 0, -1, true, false);

            for (Object logEntry : logEntries) {
                var svnLogEntry = (SVNLogEntry) logEntry;
                List<String> ticketsFromCommitMessage = VcsUtils.getTicketsFromCommitMessage(svnLogEntry.getMessage(), project);
                boolean hasFixedTicket = VcsUtils.hasFixedTicket(ticketsFromCommitMessage, project);
                var c = new Commit(project, "svn", svnLogEntry.getDate(), String.valueOf(svnLogEntry.getRevision()), ticketsFromCommitMessage, hasFixedTicket);
                revisions.add(c);
            }
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return revisions;
    }
}
