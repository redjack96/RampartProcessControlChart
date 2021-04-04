package com.giacomolorenzo.rossi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Classe utilizzata per generare le release. Poiché sono solo 13 l'output non e' stato utilizzato.
 */
public class GetReleaseInfo {

    private static HashMap<LocalDateTime, String> releaseNames;
    private static HashMap<LocalDateTime, String> releaseID;
    private static ArrayList<LocalDateTime> releaseDates;
    private static final Logger logger = Logger.getLogger(GetReleaseInfo.class.getName());

    private GetReleaseInfo(){
    }

    public static void writeReleaseInfo() {
        // Fills the arraylist with releases dates and orders them
        // Ignores releases with missing dates
        String projName = PropertyManager.loadProperties().getProperty("project");
        releaseDates = new ArrayList<>();
        int i;
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject json;
        try {
            json = readJsonFromUrl(url);
            JSONArray versions = json.getJSONArray("versions");
            releaseNames = new HashMap<>();
            releaseID = new HashMap<>();
            for (i = 0; i < versions.length(); i++) {
                String name = "";
                String id = "";
                if (versions.getJSONObject(i).has("releaseDate")) {
                    if (versions.getJSONObject(i).has("name"))
                        name = versions.getJSONObject(i).get("name").toString();
                    if (versions.getJSONObject(i).has("id"))
                        id = versions.getJSONObject(i).get("id").toString();
                    addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);
                }
            }
        } catch (IOException e) {
            logger.warning("Impossibile leggere il file json dall'url");
        }
        // order releases by date
        // @Override
        releaseDates.sort(Comparator.naturalOrder());

        if (releaseDates.size() < 6) return;

        String outname = projName + "-VersionInfo.csv";
        try(FileWriter fileWriter = new FileWriter(outname)) {
            // Name of CSV for output
            fileWriter.append("Index,Version ID,Version Name,Date");
            fileWriter.append("\n");
            for (i = 0; i < releaseDates.size(); i++) {
                int index = i + 1;
                fileWriter.append(Integer.toString(index));
                fileWriter.append(",");
                fileWriter.append(releaseID.get(releaseDates.get(i)));
                fileWriter.append(",");
                fileWriter.append(releaseNames.get(releaseDates.get(i)));
                fileWriter.append(",");
                fileWriter.append(releaseDates.get(i).toString());
                fileWriter.append("\n");
            }
            fileWriter.flush();
        } catch (Exception e) {
            logger.warning("Impossibile scrivere le release sul file csv");
        }
    }

    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();
        if (!releaseDates.contains(dateTime))
            releaseDates.add(dateTime);
        releaseNames.put(dateTime, name);
        releaseID.put(dateTime, id);
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