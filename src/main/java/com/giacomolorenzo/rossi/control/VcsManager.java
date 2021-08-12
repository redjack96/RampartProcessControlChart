package com.giacomolorenzo.rossi.control;

public interface VcsManager {

    void printBranches();

    /**
     * Scrive su un file [project]-commits.csv tutti i commit che contengono l'ID del progetto al loro interno
     * e le loro date.
     */
    void writeCommitWithTickedID();
}
