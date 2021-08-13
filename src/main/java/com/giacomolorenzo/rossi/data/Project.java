package com.giacomolorenzo.rossi.data;

import com.giacomolorenzo.rossi.control.GitManager;
import com.giacomolorenzo.rossi.control.ReleaseManager;
import com.giacomolorenzo.rossi.control.SvnManager;
import com.giacomolorenzo.rossi.control.TicketManager;
import com.giacomolorenzo.rossi.utils.DateUtils;
import com.giacomolorenzo.rossi.utils.FileUtils;
import com.giacomolorenzo.rossi.utils.ReleaseUtils;
import lombok.Data;

import java.time.YearMonth;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Classe di raccordo che rappresenta tutte le informazioni raccolte su un progetto
 */
@Data
public class Project {
    private static final Logger logger = Logger.getLogger(Project.class.getSimpleName());
    private String name;
    private ReleaseManager releaseManager;
    private TicketManager ticketManager;
    private GitManager gitManager;
    private SvnManager svnManager;
    private List<ProjectRelease> allReleases; // sono ordinate per versione!
    private List<JiraTicket> allFixedTickets;
    private List<Commit> allCommits;
    private List<Commit> allRevisions;

    /**
     * Sono inclusi tutti i bugs, anche quelli senza un commit che li risolve
     */
    private Map<ProjectRelease, Double> proportionForRelease;


    /**
     * Costruttore che colleziona e inizializza tutti i dati del progetto per poter produrre il dataset.
     *
     * @param name nome del progetto (RAMPART)
     */
    public Project(String name) {
        this.name = name;
        this.releaseManager = new ReleaseManager(this);
        this.ticketManager = new TicketManager(this);
        this.gitManager = new GitManager(true, this); // i commit vanno presi per ultimi perché richiedono release e ticket
        this.svnManager = new SvnManager(this);
        this.allReleases = this.releaseManager.findAllReleases(); // Richiede internet, se il file [name]-ReleaseInfo.csv non è presente
        logger.info(() -> name + ": Releases Done");
        this.allFixedTickets = ticketManager.findAllFixedTickets(); // Richiede internet, se il file [name]-TicketInfo.csv non è presente
        logger.info(() -> name + ": Tickets Done");
        this.allCommits = gitManager.findAllCommits();
        this.allRevisions = svnManager.findAllRevisions();
        logger.info(() -> name + ": Commits Done ");
    }

    public String toString() {
        return this.name;
    }

    /**
     * Ricava i dati delle release per il csv
     *
     * @return lista di stringhe con i dati del csv
     */
    public List<ProcessControlChartData> getReleaseDataForPCC() {
        Set<String> ticketIdWithCommit = new HashSet<>();
        List<ProcessControlChartData> pccDataList = new ArrayList<>();

        for (ProjectRelease release : getAllReleases()) {
            // trovo i commit git della release corrente (anche quelli che non fixano nulla)
            var commitOfRelease = getAllCommits().stream()
                    .filter(commit -> release.containsCommitDate(commit.getCommitDate()))
                    .collect(Collectors.toList());
            // trovo le revisioni svn della release corrente (anche quelle che non fixano nulla)
            var revisionOfRelease = getAllRevisions().stream()
                    .filter(revision -> release.containsCommitDate(revision.getCommitDate()))
                    .collect(Collectors.toList());
            // metto tutto in un' unica lista
            commitOfRelease.addAll(revisionOfRelease);
            // numero di ticket fixed relativi a un commit o revisione per la release corrente
            var fixedTicketWithCommit = commitOfRelease.stream()
                    .filter(Commit::isHasFixedTicket)
                    .flatMap(commit -> commit.getTickets().stream()) // passo da lista (stream) di liste di ticket a un' unica lista (stream) di ticket
                    .filter(o -> !ticketIdWithCommit.contains(o)) // Importante: escludo i ticket che sono già stati contati in un altro commit.
                    .distinct() // elimino i ticket duplicati
                    .collect(Collectors.toList());

            ticketIdWithCommit.addAll(fixedTicketWithCommit);

            var pccData = new ProcessControlChartData(this);
            pccData.setReleaseDate(DateUtils.toYearMonthDayString(release.getReleaseDate())); // releaseDate
            pccData.setCommitInRelease(commitOfRelease.size());
            pccData.setVersionName(release.getReleaseName());
            pccData.setVersion(release.getId()); // dalla release
            pccData.setFixedTicketWithCommit(fixedTicketWithCommit.size()); // ticket fixed con almeno un commit
            pccDataList.add(pccData);
        }

        // Imposto i dati comuni
        // ATTENZIONE: Escludo dalla media e stdDev le release che non hanno alcun commit o revisione.
        var average = pccDataList.stream()
                .filter(processControlChartData -> processControlChartData.getCommitInRelease() > 0)
                .mapToLong(ProcessControlChartData::getFixedTicketWithCommit)
                .average()
                .orElse(0.0);
        var population = pccDataList.stream()
                .filter(processControlChartData -> processControlChartData.getCommitInRelease() > 0)
                .mapToDouble(ProcessControlChartData::getFixedTicketWithCommit)
                .toArray();
        var stdDev = populationStandardDeviation(population, average);
        logger.info(() -> "Deviazione Standard = " + stdDev);
        for (var pccData : pccDataList) {
            var upperBound = average + 3 * stdDev;
            var lowerBound = Math.max(0.0, average - 3 * stdDev);
            pccData.setAverageTicketPerRelease(average); // calcolare la media di ticket risolti per la release
            pccData.setUpperBound(upperBound); // media +3 stdDev
            pccData.setLowerBound(lowerBound); // media -3 stdDev oppure 0 se negativa
        }

        logger.info(() -> "Ticket totali con commit = " + ticketIdWithCommit.size());

        return pccDataList;
    }

    public List<MonthProcessControlChartData> getMonthDataForPCC() {
        List<MonthProcessControlChartData> pccDataList = new ArrayList<>();

        Date minDate = allFixedTickets.stream().map(JiraTicket::getResolutionDate).min(Comparator.naturalOrder()).orElseThrow();
        Date maxDate = allFixedTickets.stream().map(JiraTicket::getResolutionDate).max(Comparator.naturalOrder()).orElseThrow();

        List<YearMonth> yearMonthList = DateUtils.getYearMonthListFromDateRange(minDate, maxDate);
        for (YearMonth yearMonth : yearMonthList) {
            long fixedTicketsInMonth = allFixedTickets.stream()
                    .filter(ticket -> ticket.isInMonth(yearMonth))
                    .filter(jiraTicket -> allCommits.stream()
                            .flatMap(commit -> commit.getTickets().stream())
                            .anyMatch(ticketId -> jiraTicket.getTicketID().equals(ticketId)))
                    .count();
            // numero di commit e revisioni, compresi quelli che non risolvono nulla
            var numberOfCommitInMonth = getAllCommits().stream()
                    .filter(commit -> DateUtils.isInMonth(commit.getCommitDate(), yearMonth))
                    .count() +
                    getAllRevisions().stream()
                            .filter(commit -> DateUtils.isInMonth(commit.getCommitDate(), yearMonth))
                            .count();
            var monthPcc = new MonthProcessControlChartData(this);
            var versionId = ReleaseUtils.getVersionIdFromCommitDate(DateUtils.toDate(yearMonth.atDay(1)), this);
            if(versionId != -1) { // Importante: escludo le settimane che fanno parte di una release ancora non rilasciata.
                monthPcc.setVersion(versionId);
                monthPcc.setVersionName(ReleaseUtils.findReleaseById(monthPcc.getVersion(), this).getReleaseName());
                monthPcc.setYearMonthOfResolution(yearMonth.toString());
                monthPcc.setFixedTicketWithCommit(fixedTicketsInMonth);
                monthPcc.setCommitInMonth(numberOfCommitInMonth);
                pccDataList.add(monthPcc);
            }
        }

        // Imposto i dati comuni
        // ATTENZIONE: Escludo dalla media e stdDev le release che non hanno alcun commit o revisione.
        var average = pccDataList.stream()
                .filter(monthProcessControlChartData -> monthProcessControlChartData.getCommitInMonth() > 0)
                .mapToLong(MonthProcessControlChartData::getFixedTicketWithCommit)
                .average()
                .orElse(0.0);
        var population = pccDataList.stream()
                .filter(processControlChartData -> processControlChartData.getCommitInMonth() > 0)
                .mapToDouble(MonthProcessControlChartData::getFixedTicketWithCommit)
                .toArray();
        var stdDev = populationStandardDeviation(population, average);
        logger.info(() -> "Deviazione Standard = " + stdDev);
        for (var pccData : pccDataList) {
            var upperBound = average + 3 * stdDev;
            var lowerBound = Math.max(0.0, average - 3 * stdDev);
            pccData.setAverageTicketPerMonth(average); // calcolare la media di ticket risolti per la release
            pccData.setUpperBound(upperBound); // media +3 stdDev
            pccData.setLowerBound(lowerBound); // media -3 stdDev oppure 0 se negativa
        }


        return pccDataList;
    }

    /**
     * Calcola la deviazione standard della popolazione di valori
     * @param values l' intera popolazione dei valori
     * @return deviazione standard della popolazione
     */
    public static strictfp double populationStandardDeviation(double[] values, double mean) {
        double n = values.length;
        double dv = 0;
        for (double d : values) {
            double dm = d - mean;
            dv += dm * dm;
        }
        return Math.sqrt(dv / n);
    }

    /**
     * Produce il file csv per il progetto rappresentato dall' istanza corrente
     */
    public void makeCSV() {
        logger.log(Level.INFO, "Sto creando il dataset completo delle release per il progetto {0}", name);
        FileUtils.writeCSV(getReleaseDataForPCC(), this, ProcessControlChartData.class);
    }

    public void makeMonthCSV() {
        logger.log(Level.INFO, "Sto creando il dataset completo dei mesi per il progetto {0}", name);
        FileUtils.writeCSV(getMonthDataForPCC(), this, MonthProcessControlChartData.class);
    }

}
