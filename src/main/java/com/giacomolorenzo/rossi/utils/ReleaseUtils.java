package com.giacomolorenzo.rossi.utils;

import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.data.ProjectRelease;

import java.util.Date;
import java.util.List;

public class ReleaseUtils {

    private ReleaseUtils(){}

    public static ProjectRelease findReleaseByName(String versionName, Project project) {
        List<ProjectRelease> releases = project.getAllReleases();
        for (ProjectRelease release : releases) {
            if (release.getReleaseName().equals(versionName)) {
                return release;
            }
        }
        // Esempio: AVRO 1.7.6 non è una release rilasciata, ma esiste nelle AV in Jira. in tal caso bisognerebbe restituire null
        return new ProjectRelease(-1, "Unknown", new Date(), project);
    }

    public static ProjectRelease findReleaseById(int versionInt, Project project) {
        List<ProjectRelease> releases = project.getAllReleases();
        for (ProjectRelease release : releases) {
            if (release.getId() == versionInt) {
                return release;
            }
        }
        return new ProjectRelease(-1, "Unknown", null, project); // N.B: per le release non ancora rilasciate è normale che ritorna -1.
    }

    /**
     * Restituisce l'indice corrispondente alla release a cui un commit appartiene.
     * Esempio: il commit è stato fatto il 01/04/2018
     * La release 3 e' stata rilasciata il 23/03/2018
     * La release 4 e' stata rilasciata il 11/06/2018
     * Il commit appartiene alla release 4
     * @return -1 se il commit non appartiene a nessuna release rilasciata
     */
    public static int getVersionIdFromCommitDate(Date commitDate, Project project) {
        for (var release : project.getAllReleases()) {
            if (release.containsCommitDate(commitDate)) {
                return release.getId(); //Restituisce l'id interno (1,2,...ecc), non l'ID di Jira
            }
        }
        return -1;
    }

    /**
     * Restituisce l'indice corrispondente alla release a cui un ticket appartiene.
     * Esempio: il ticket è stato aperto il 01/04/2018
     * La release 3 e' stata rilasciata il 23/03/2018
     * La release 4 e' stata rilasciata il 11/06/2018
     * Il ticket ha OV = 4 (non 3)
     * @return -1 se il ticket non appartiene a nessuna release rilasciata
     */
    public static int getVersionIdFromTicketOpeningDate(Date ticketDate, Project project) {
        return getVersionIdFromCommitDate(ticketDate, project);
    }

    /**
     * Ricava la release di un commit a partire dalla data di creazione del commmit o del ticket.
     * Se un commit avviene dopo il rilascio della release R, faraa' parte della release R+1.
     *
     * Esempio
     * versione 3 2013-01-01
     * versione 4 2014-01-01
     *
     * commit a 2013-05-05 -> appartiene alla versione 4
     * commit b 2014-06-06 -> appartiene alla versione 5
     *
     * ticket opened 2013-05-05 -> appartiene alla versione 3
     * ticket opened 2014-06-06 -> appartiene alla versione 4
     *
     * @param date la data di un commit o la data di creazione di un ticket
     * @param thisProject il progetto a cui appartiene il commit
     * @return la ProjectRelease a cui appartiene il commit.
     */
    public static ProjectRelease getReleaseOfCommitFromDate(Date date, Project thisProject) {
        int versionIdFromDate = getVersionIdFromCommitDate(date, thisProject);
        return ReleaseUtils.findReleaseById(versionIdFromDate, thisProject);
    }

    /**
     * Se la data di apertura del ticket e' avvenuta dopo il rilascio della release R, il ticket ha OV = R
     * @param ticketOpeningDate data di apertura del ticket
     * @param thisProject il progetto di cui ricavare la release di appartenenza del ticket
     * @return La projectRelease a cui un ticket appartiene data la ticketOpeningDate
     */
    public static ProjectRelease getReleaseOfTicketFromOpeningDate(Date ticketOpeningDate, Project thisProject) {
        int versionIdFromDate = getVersionIdFromTicketOpeningDate(ticketOpeningDate, thisProject);
        return ReleaseUtils.findReleaseById(versionIdFromDate, thisProject);
    }
}
