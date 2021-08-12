package com.giacomolorenzo.rossi.data;

import com.giacomolorenzo.rossi.utils.DateUtils;
import lombok.Data;

import java.util.*;

@Data
public class ProjectRelease extends Writable implements Readable{
    private static final String RELEASE_FILE_SUFFIX = "-ReleaseInfo.csv";
    int id;
    Project project;
    int officialId;
    String releaseName;
    Date releaseDate;
    Date prevRelease;
    boolean firstVersion;
    boolean lastVersion;

    /**
     * Per i test
     *
     * @param project null
     */
    public ProjectRelease(Project project) {
        this.project = project;
    }

    public ProjectRelease(int id, String releaseName, Date releaseDate, Project project) {
        this.id = id;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
        this.project = project;
    }

    /**
     * Verifica se la data di un commit è compresa in una release.
     *
     * @param commitDate La data del RevCommit necessaria per determinare se appartiene a questa istanza di release
     * @return true se la data del commit è compresa tra la data della release precedente e la data di questa release
     */
    public boolean containsCommitDate(Date commitDate) {
        if (firstVersion) {
            return commitDate.before(releaseDate); // se è la prima versione, il commit deve essere prima della sua data di rilascio
        }
        // IMPORTANTE: Se la data è lo stesso giorno della release R-1, dico che la data è contenuta nella release R, perché non ho l' orario della release
        if (commitDate.equals(prevRelease)) {
            return true;
        }
        return commitDate.after(prevRelease) && commitDate.before(releaseDate);
    }

    public ProjectRelease loadFromCSV(String[] csvRow) {
        var p = new ProjectRelease(project);
        p.setId(Integer.parseInt(csvRow[0]));
        p.setOfficialId(Integer.parseInt(csvRow[2]));
        p.setReleaseName(csvRow[3]);
        p.setReleaseDate(DateUtils.getDateFromYearMonthDayString(csvRow[4]));
        p.setPrevRelease(csvRow[5].equals("null") ? null : DateUtils.getDateFromYearMonthDayString(csvRow[5]));
        p.setFirstVersion(Boolean.parseBoolean(csvRow[6]));
        p.setLastVersion(Boolean.parseBoolean(csvRow[7]));
        return p;
    }

    public String getFileNameSuffix() {
        return RELEASE_FILE_SUFFIX;
    }

    @Override
    public String toString() {
        return releaseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectRelease that = (ProjectRelease) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
