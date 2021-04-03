package com.giacomolorenzo.rossi;

import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveTicketsID {

    private static final Logger logger = Logger.getLogger(RetrieveTicketsID.class.getName());

    private RetrieveTicketsID() {
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public static String writeFixedIssues() {
        int j;
        int i = 0;
        int total;
        String projName = PropertyManager.loadProperties().getProperty("project");
        File file = new File(projName + "-TicketsID.csv");
        if(!file.exists()) {
            try {
                if(file.createNewFile()) logger.info(file.getName() + "Created");
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(file)),
                ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)){
            //Get JSON API for closed bugs w/ AV in the project
            do {
                //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
                j = i + 1000;
                String url = "https://issues.apache.org/jira/rest/api/2/search?" +
                        "jql=project=%22" + projName +                   // Nome del progetto
                        "%22AND(%22status%22=%22closed%22OR%22status%22=%22resolved%22)" +  // solo i ticket con status == closed or status == resolved
                        "AND%22resolution%22=%22fixed%22" +              // solo i ticket con resolution == fixed
                        "%20ORDER%20BY%20key%20ASC" +                     // [NEW] Ordinamento crescente
                        "&fields=key,resolutiondate,versions,created" +  // campi da includere nel risultato JSON
                        "&startAt=" + i +                                // indice iniziale da cui stampare
                        "&maxResults=" + j;                              // numero massimo di risultati (1000)
                JSONObject json = readJsonFromUrl(url);
                JSONArray issues = json.getJSONArray("issues");
                total = json.getInt("total");

                csvWriter.writeNext(new String[]{"key", "resolutionDate"});
                for (; i < total && i < j; i++) {
                    //Iterate through each bug
                    String resolutionDateJira = issues.getJSONObject(i % 1000).getJSONObject("fields").getString("resolutiondate");
                    // Pattern-Matching della data
                    Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
                    Matcher dateMatcher = datePattern.matcher(resolutionDateJira);
                    String resolutionDate = resolutionDateJira;
                    if(dateMatcher.find()) {
                        resolutionDate = dateMatcher.group(0);
                    } else{
                        String messaggio = "pattern non trovato per " + resolutionDateJira;
                        logger.warning(messaggio);
                    }
                    String key = issues.getJSONObject(i % 1000).get("key").toString();
                    csvWriter.writeNext(new String[]{key, resolutionDate});
                }
            } while (i < total);
        } catch(IOException io){
            io.printStackTrace();
        }
        return file.getName();
    }
}
