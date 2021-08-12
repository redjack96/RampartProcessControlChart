package com.giacomolorenzo.rossi.control;

import com.giacomolorenzo.rossi.data.JiraTicket;
import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.data.ProjectRelease;
import com.giacomolorenzo.rossi.utils.DateUtils;
import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class TicketManager {

    private static final Logger logger = Logger.getLogger(TicketManager.class.getName());
    private final Project project;

    public TicketManager(Project project) {
        this.project = project;
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

    // FIXME: non devo usare la resolution date ma semplicemente controllare se il commit ha l'id del ticket. Se non c'è lo scarto.
    public List<JiraTicket> findAllTickets() {
        int j;
        int i = 0;
        int total;
        String projName = project.getName();
        File file = new File(projName + "-TicketsID.csv");
        List<JiraTicket> tickets = new ArrayList<>();
        if (!file.exists()) {
            try {
                if (file.createNewFile()) logger.info(file.getName() + "Created");
            } catch (IOException e) {
                logger.severe("Impossibile creare il file per i ticket fixed");
                return tickets;
            }
        }

        //Get JSON API for closed bugs w/ AV in the project
        try {
            do {
                //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
                j = i + 1000;
                String url = "https://issues.apache.org/jira/rest/api/2/search?" +
                        "jql=project=%22" + projName +                   // Nome del progetto
                        "%22AND(%22status%22=%22closed%22OR%22status%22=%22resolved%22)" +  // solo i ticket con status == closed or status == resolved
                        "AND%22resolution%22=%22fixed%22" +              // solo i ticket con resolution == fixed
                        "%20ORDER%20BY%20key%20ASC" +                     // [NEW] Ordinamento crescente
                        "&fields=key,resolutiondate,versions,created,issuetype" +  // campi da includere nel risultato JSON
                        "&startAt=" + i +                                // indice iniziale da cui stampare
                        "&maxResults=" + j;                              // numero massimo di risultati (1000)
                JSONObject json = readJsonFromUrl(url);
                JSONArray issues = json.getJSONArray("issues");
                total = json.getInt("total");

                for (; i < total && i < j; i++) {
                    //Iterate through each bug
                    String resolutionDateJira = issues.getJSONObject(i % 1000).getJSONObject("fields").getString("resolutiondate");
                    // Pattern-Matching della data
                    Date resolutionDate = DateUtils.getDateFromYearMonthDayString(resolutionDateJira);
                    String ticketId = issues.getJSONObject(i % 1000).get("key").toString();
                    JiraTicket jiraTicket = new JiraTicket(ticketId, project);
                    jiraTicket.setResolution("fixed"); // perché li ho filtrati nella query
                    jiraTicket.setType(issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONObject("issuetype").getString("name"));
                    jiraTicket.setResolutionDate(resolutionDate); // necessario per capire a quale release appartiene
                    tickets.add(jiraTicket);
                }
            } while (i < total);
        } catch (IOException io) {
            logger.severe("Impossibile trovare i fixed tickets");
        }
        return tickets;
    }
}
