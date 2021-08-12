package com.giacomolorenzo.rossi.data;

import com.giacomolorenzo.rossi.control.GitManager;
import com.giacomolorenzo.rossi.control.ReleaseManager;
import com.giacomolorenzo.rossi.control.SvnManager;
import com.giacomolorenzo.rossi.control.TicketManager;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private List<JiraTicket> allFixedJiraBugs;
    private List<Commit> allCommits;

    /**
     * Sono inclusi tutti i bugs, anche quelli senza un commit che li risolve
     */
    private Map<ProjectRelease, Double> proportionForRelease;


    /**
     * Costruttore che colleziona e inizializza tutti i dati del progetto per poter produrre il dataset.
     * @param name              nome del progetto (RAMPART)
     */
    public Project(String name) {
        this.name = name;
        this.releaseManager = new ReleaseManager(this);
        this.ticketManager = new TicketManager(this);
        //TODO
        this.gitManager = new GitManager(true,"TODO",this); // i commit vanno presi per ultimi perché richiedono release e ticket
        this.svnManager = new SvnManager();
        this.allReleases = this.releaseManager.findAllReleases(); // Richiede internet, se il file [name]-ReleaseInfo.csv non è presente
        logger.info(() -> name + ": Releases Done");
        this.allFixedJiraBugs = ticketManager.findAllTickets(); // Richiede internet, se il file [name]-TicketInfo.csv non è presente
        logger.info(() -> name + ": Tickets Done");
        // this.allCommits = gitManager.findAllCommits(); // TODO
        // this.allCommits.addAll(svnManager.findAllRevisions()); // TODO
        logger.info(() -> name + ": Commits Done ");
    }

    public String toStringAll() {
        return this.name + ": " + allReleases.size() + " releases e " + allFixedJiraBugs.size() + " fixedBugs";
    }

    public String toString() {
        return this.name;
    }

    /**
     * Ricava i dati per il csv
     * @return lista di stringhe con i dati del csv
     */
    public List<String[]> getCSVData() {
        return new ArrayList<>();
    }

    /**
     * Produce il file csv per il progetto rappresentato dall' istanza corrente
     */
    public void makeCSV() {
        logger.log(Level.INFO, "Sto creando il dataset completo per il progetto {0}", name);
        // FileUtils.writeCSV(getCSVData(), this, ClassData.class);
    }

}
