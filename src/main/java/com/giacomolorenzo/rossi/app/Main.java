package com.giacomolorenzo.rossi.app;

import com.giacomolorenzo.rossi.data.Commit;
import com.giacomolorenzo.rossi.data.JiraTicket;
import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.data.ProjectRelease;
import com.giacomolorenzo.rossi.utils.FileUtils;
import com.giacomolorenzo.rossi.utils.ReleaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.giacomolorenzo.rossi.utils.Constants.RAMPART;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Attenzione: Il progetto RAMPART utilizza SVN. Non essendo riuscito a fare git svn checkout, per mancanza di memoria,
     * ho dovuto recuperare la storia di RAMPART da un Mirror presente su GitHub per utilizzare JGit.
     *
     * @param args niente
     */
    public static void main(String[] args) {
        // [timestamp] package.className methodName() Severity: the message & the stacktrace if present\n
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %2$s() %4$s: %5$s%6$s%n");
        logger.info("Proprietà caricate con successo");

        Project rampart = new Project(RAMPART);
        FileUtils.writeCSV(rampart.getAllReleases(), rampart, ProjectRelease.class);
        FileUtils.writeCSV(rampart.getAllFixedTickets(), rampart, JiraTicket.class);

        // metto tutti i commit e le revisions in un' unica lista.
        List<Commit> allCommits = new ArrayList<>(rampart.getAllCommits());
        allCommits.addAll(rampart.getAllRevisions());

        FileUtils.writeCSV(allCommits, rampart, Commit.class);
        rampart.makeCSV();
        rampart.makeMonthCSV();

        // Informazioni utili
        int totalGitCommits = rampart.getAllCommits().size();
        logger.info(() -> "Numero di commit git: " + totalGitCommits);
        int totalSvnRevisions = rampart.getAllRevisions().size();
        logger.info(() -> "Numero di revisions svn: " + totalSvnRevisions);
        int totalCommits = totalGitCommits + totalSvnRevisions;
        logger.info(() -> "Numero totale di commit/revisioni: " + totalCommits);

        long gitCommitWithFixedTicket = rampart.getAllCommits().stream().filter(Commit::isHasFixedTicket).count();
        long svnRevisionsWithFixedTicket = rampart.getAllRevisions().stream().filter(Commit::isHasFixedTicket).count();
        logger.info(() -> String.format("Numero di commit git con almeno un fixed ticket nel messaggio: %d. %f %% sul totale dei commit git. %f %% sul totale dei commit/revisions",
                gitCommitWithFixedTicket, 100.0 * gitCommitWithFixedTicket / totalGitCommits, 100.0 * gitCommitWithFixedTicket / totalCommits));
        logger.info(() -> String.format("Numero di revisioni svn con almeno un fixed ticket nel messaggio: %d. %.2f %% sul totale delle revisioni svn. %.2f %% sul totale dei commit/revisions",
                svnRevisionsWithFixedTicket, 100.0 * svnRevisionsWithFixedTicket / totalSvnRevisions, 100.0 * svnRevisionsWithFixedTicket / totalCommits));
        var ticketRisoltiDaGit = rampart.getAllCommits().stream().flatMap(commit -> commit.getTickets().stream()).distinct().collect(Collectors.toList());
        var ticketRisoltiDaSvn = rampart.getAllRevisions().stream().flatMap(revision -> revision.getTickets().stream()).distinct().collect(Collectors.toList());

        long svnExclusives = ticketRisoltiDaSvn.stream().filter(ticketSvn -> !ticketRisoltiDaGit.contains(ticketSvn)).count();
        long gitExclusives = ticketRisoltiDaGit.stream().filter(ticketGit -> !ticketRisoltiDaSvn.contains(ticketGit)).count();

        logger.info(() -> String.format("Numero di revisioni svn che hanno risolto un ticket mai risolto da git: %d", svnExclusives));
        logger.info(() -> String.format("Numero di commit git che hanno risolto un ticket mai risolto da svn: %d", gitExclusives));

        var fixedTicketWithCommit = rampart.getAllFixedTickets().stream()
                .filter(jiraTicket -> rampart.getAllCommits().stream()
                        .flatMap(commit -> commit.getTickets().stream())
                        .anyMatch(ticket -> ticket.equals(jiraTicket.getTicketID())))
                .collect(Collectors.toList());
        var fixedTicketsWithCommitUnreleasedVersion = fixedTicketWithCommit.stream()
                .filter(jiraTicket -> ReleaseUtils.getReleaseOfCommitFromDate(jiraTicket.getResolutionDate(), rampart).getId() == -1)
                .count();
        var numberOfFixedTicketWithMissingCommit = rampart.getAllFixedTickets().size() - fixedTicketWithCommit.size();
        logger.info(() -> String.format("Numero di JiraTicket che hanno resolution = fixed e che hanno almeno un commit con il suo ticket id nel messaggio %d (di cui %d fixati in una release non ancora rilasciata), sul totale %.2f %%", fixedTicketWithCommit.size(), fixedTicketsWithCommitUnreleasedVersion, 100.0 * fixedTicketWithCommit.size() / rampart.getAllFixedTickets().size()));
        logger.info(() -> String.format("Numero di JiraTicket che hanno resolution = fixed ma che non hanno nessun commit con il suo ticket id nel messaggio %d, sul totale %.2f %%", numberOfFixedTicketWithMissingCommit, 100.0 * numberOfFixedTicketWithMissingCommit / rampart.getAllFixedTickets().size()));

    }
}
