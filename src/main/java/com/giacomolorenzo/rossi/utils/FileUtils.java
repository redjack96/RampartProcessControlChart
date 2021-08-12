package com.giacomolorenzo.rossi.utils;

import com.giacomolorenzo.rossi.control.GitManager;
import com.giacomolorenzo.rossi.data.Project;
import com.giacomolorenzo.rossi.data.Readable;
import com.giacomolorenzo.rossi.data.ReadableWritable;
import com.giacomolorenzo.rossi.data.Writable;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.giacomolorenzo.rossi.utils.Constants.CSV_SEPARATOR;
import static com.giacomolorenzo.rossi.utils.Constants.CSV_SEPARATOR_STR;

public class FileUtils {
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    public static boolean isEmpty(File directory) {
        var path = directory.toPath();
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            } catch (IOException io) {
                logger.severe("Errore nel controllo sulla directory " + directory);
            }
        }
        return false;
    }

    /**
     * Restituisce l' estensione senza il punto.
     */
    public static String getExtensionWithoutPoint(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1)).orElse("");
    }

    /**
     * Restituisce l' estensione del file con il punto.
     *
     * @param filename nome del file
     * @return estensione del file con "." davanti.
     */
    public static String getExtension(String filename) {
        return "." + getExtensionWithoutPoint(filename);
    }

    public static String getFileNameWithoutExtension(String fileNameWithExtension) {
        if (!fileNameWithExtension.contains(".")) return fileNameWithExtension;
        return fileNameWithExtension.substring(0, getStartIndexOfExtension(fileNameWithExtension) - 1);
    }

    private static int getStartIndexOfExtension(String fileNameWithExtension) {
        char[] charArray = fileNameWithExtension.toCharArray();
        int startIndexOfExtension = 0;
        for (int i = charArray.length - 1; i >= 0; i--) {
            char c = charArray[i];
            if (c == '.') {
                startIndexOfExtension = i + 1;
                break;
            }
        }
        return startIndexOfExtension;
    }

    /**
     * Cambia l' estensione nel nome di un file.
     * Se il file non ha alcuna estensione, gli aggiunge l' estensione specificata.
     *
     * @param fileName     il nome del file compresa l' estensione
     * @param newExtension la nuova estensione del file (con o senza il ".")
     * @return una nuova stringa con il vecchio nome del file e la nuova estensione.
     */
    public static String changeExtension(String fileName, String newExtension) {
        if (newExtension.contains(".")) return getFileNameWithoutExtension(fileName) + newExtension;
        else if (newExtension.equals("")) return getFileNameWithoutExtension(fileName);
        return getFileNameWithoutExtension(fileName) + "." + newExtension;
    }

    public static String appendEndOfFileName(String oldName, String append) {
        if (oldName.contains(".") && append.contains(".")) throw new IllegalArgumentException("la stringa da aggiungere non può contenere un punto se questo è già presente nel nome iniziale");
        else if (oldName.contains(".") && !append.contains(".")) return getFileNameWithoutExtension(oldName) + append + getExtension(oldName);
        else return oldName + append;
    }

    /**
     * Determina se due file hanno la estensione specificate (SENZA IL PUNTO)
     *
     * @param file1     pathName con estensione del primo file
     * @param file2     pathName con estensione del secondo file
     * @param extension l' estensione SENZA il punto
     * @return true, se l' estensione è quella specificata per entrambi i file.
     */
    public static boolean sameExtension(String file1, String file2, String extension) {
        return extension.equals(getExtensionWithoutPoint(file1)) && extension.equals(getExtensionWithoutPoint(file2));
    }

    public static boolean isJavaFile(String fileName) {
        return getExtensionWithoutPoint(fileName).equals("java");
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return new JSONObject(sb.toString());
        } catch (UnknownHostException unk) {
            logger.severe("Impossibile collegarsi a internet...");
            return null;
        }
    }

    public static File writeCSVSimple(List<String[]> dataList, String fileName, String[] header) throws IOException {
        var f = new File(fileName);
        if (f.exists()) return f;
        else if (!f.createNewFile()) {
            throw new IOException("Impossibile creare il file CSV da scrivere");
        }

        try (var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(f)),
                CSV_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            csvWriter.writeNext(header);
            var i = 0;
            for (var datum : dataList) {
                csvWriter.writeNext(datum);
                // Ogni tanto aggiorno il file con i dati scritti finora.
                if (i++ % dataList.size() / 10 == 0) {
                    csvWriter.flush();
                }
            }
            Logger.getLogger(GitManager.class.getSimpleName()).info(() -> String.format("Righe scritte in %s: %d", fileName, dataList.size()));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            try {
                Files.delete(f.getAbsoluteFile().toPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return f;
    }

    /**
     * Permette di scrivere un csv data una lista di oggetti, il loro tipo di dato e il progetto a cui si riferiscono.
     *
     * @param dataList la lista di oggetti da scrivere
     * @param project il progetto a cui si riferisce il csv
     * @param type il tipo di dato da scrivere
     * @param <T> il tipo di dato Writable
     */
    public static <T extends Writable> void writeCSV(List<T> dataList, Project project, Class<T> type) {
        String csvName = ReadableWritable.getCsvName(project, type);
        var f = new File(csvName);
        if (f.exists()) {
            logger.info(() -> String.format("%s esiste gia'. Non scrivo nulla", csvName));
            return;
        }
        try {
            boolean newFile = f.createNewFile();
            if (!newFile) {
                throw new IllegalArgumentException("Il nome del file non e' valido");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (var csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(f)),
                CSV_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            String[] csvHeader = T.getCSVHeader(type, project);
            csvWriter.writeNext(csvHeader);
            var i = 0;
            for (var datum : dataList) {
                csvWriter.writeNext(datum.getValues());
                // Ogni tanto aggiorno il file con i dati scritti finora.
                if (i++ % dataList.size() / 10 == 0) {
                    csvWriter.flush();
                }
            }
            Logger.getLogger(GitManager.class.getSimpleName()).info(() -> String.format("Righe scritte in %s: %d", csvName, dataList.size()));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            try {
                Files.delete(f.getAbsoluteFile().toPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Legge un file CSV, se esiste, e carica i suoi contenuti nelle relative classi.
     *
     * @param type    tipo della classe che implementa Readable
     * @param project il progetto a cui si riferisce il CSV
     * @return la lista di oggetti T letti
     */
    public static <T extends Readable> List<T> readCSV(Class<T> type, Project project) {
        String csvName = ReadableWritable.getCsvName(project, type);
        List<T> result = new ArrayList<>();
        var f = new File(csvName);
        if (!f.exists()) {
            logger.warning(() -> "Attenzione: si sta tentando di leggere un file inesistente: " + csvName);
            return result;
        }

        final CSVParser parser = new CSVParserBuilder()
                .withSeparator(CSV_SEPARATOR)
                .withIgnoreQuotations(true)
                .build();

        try (var csvReader = new CSVReaderBuilder(new BufferedReader(new FileReader(f)))
                .withSkipLines(1)
                .withCSVParser(parser)
                .build()) {


            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                var data = Readable.staticLoadFromCsv(nextRecord, type, project);
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Data una stringa, che rappresenta un array, la analizza e la trasforma in una lista di stringhe.
     *
     * @param listString array rappresentato come stringa.
     * @return List<String> con i contenuti dell' array
     */
    public static List<String> readListFromArrayString(String listString) {
        if (listString.equals("[]")) {
            return new ArrayList<>();
        }
        var substring = listString.substring(1, listString.length() - 1); // Tolgo le [ ]
        String[] split = substring.split(", ");
        return Arrays.asList(split);
    }

    public static String[] readCsvHeader(String fileName) {
        try (var brText = new BufferedReader(new FileReader(fileName))) {
            String text = brText.readLine();
            // Stop. text is the first line.
            return text.split(String.valueOf(CSV_SEPARATOR));
        } catch (IOException e) {
            e.printStackTrace();
            return new String[]{};
        }
    }
}
