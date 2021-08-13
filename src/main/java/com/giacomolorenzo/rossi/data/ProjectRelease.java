package com.giacomolorenzo.rossi.data;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectRelease extends Writable{
    private static final String RELEASE_FILE_SUFFIX = "-ReleaseInfo.csv";
    String project;
    int officialId;
    String releaseName;
    int id;
    Date releaseDate;
    Date prevRelease;
    boolean firstVersion;
    boolean lastVersion;

    public ProjectRelease(Project project){
        this.project = project.getName();
    }

    public ProjectRelease(int id, String releaseName, Date releaseDate, Project project) {
        this.project = project.getName();
        this.id = id;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
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
