package com.giacomolorenzo.rossi.control;

import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.data.ProjectRelease;
import com.giacomolorenzo.rossi.utils.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Classe utilizzata per generare le release. Poiché sono solo 13 l'output non e' stato utilizzato.
 */
public class ReleaseManager {
    private static final Logger logger = Logger.getLogger(ReleaseManager.class.getName());
    private final Project project;

    public ReleaseManager(Project project){
        this.project = project;
    }

    public List<ProjectRelease> findAllReleases() {
        // Fills the arraylist with releases dates and orders them
        // Ignores releases with missing dates
        String projName = project.getName();
        List<ProjectRelease> releases = new ArrayList<>();
        int i;
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject json;
        try {
            json = readJsonFromUrl(url);
            JSONArray versions = json.getJSONArray("versions");
            for (i = 0; i < versions.length(); i++) {
                String name = "";
                String id = "";
                if (versions.getJSONObject(i).has("releaseDate")) {
                    if (versions.getJSONObject(i).has("name"))
                        name = versions.getJSONObject(i).get("name").toString();
                    if (versions.getJSONObject(i).has("id"))
                        id = versions.getJSONObject(i).get("id").toString();

                    String jiraDate = versions.getJSONObject(i).get("releaseDate").toString();
                    var releaseDate = DateUtils.getDateFromYearMonthDayString(jiraDate);
                    var release = new ProjectRelease(i+1,name,releaseDate,project);
                    release.setOfficialId(Integer.parseInt(id));
                    releases.add(release);
                }
            }
        } catch (IOException e) {
            logger.warning("Impossibile leggere il file json dall'url");
        }
        releases.sort(Comparator.comparing(ProjectRelease::getReleaseDate));
        initializeReleases(releases);
        return releases;
    }

    private void initializeReleases(List<ProjectRelease> releases) {
        int i;
        i = 0;
        ProjectRelease previousRelease = null;
        for (ProjectRelease projectRelease : releases) {
            if (previousRelease != null) {
                projectRelease.setPrevRelease(previousRelease.getReleaseDate());
            }
            projectRelease.setFirstVersion(i == 0); // solo la prima viene impostato a true
            projectRelease.setLastVersion(i == releases.size() - 1); // solo l' ultima viene impostata a true
            i++;
            projectRelease.setId(i); // i parte da 1
            previousRelease = projectRelease;
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}