package com.lux.crewmatch.services;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.lux.crewmatch.entities.Candidate;

public class CSVHelper {

    public static String TYPE = "text/csv";

    private static final String[] HEADERS = {
            "Timestamp",
            "Email Address",
            "What is your name? (first and last)",
            "How many YEARS have you been a student at UW, including this year? (for example, a sophomore would enter \"2\")",
            "How many QUARTERS have you been in LUX Film Production Club, including this one? (for example, a new LUX member would enter \"1\")",
            "First choice in production:",
            "Second choice in production:",
            "Third choice in production:",
            "First choice in role:",
            "Second choice in role:",
            "Third choice in role:",
            "Would you rather have your preferred ROLE or your preferred PRODUCTION?"

    };

    /*
     *  Checks the validity of an uploaded file.
     *  Params:
     *      MultipartFile file - a csv file to be checked.
     *  Returns:
     *      A boolean indicating whether the uploaded file is a csv.
     */
    private static boolean isValidFile(MultipartFile file) {

        return TYPE.equals(file.getContentType());

    }

    public static List<Candidate> csvToCandidates(InputStream inputStream) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<Candidate> candidates = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                // Create a candidate with the fields from the csv
                Candidate candidate = new Candidate();
                candidate.setName(csvRecord.get(HEADERS[2]));
                candidate.setEmail(csvRecord.get(HEADERS[1]));
                candidate.setTimestamp(csvRecord.get(HEADERS[0]));
                candidate.setYearsInUW(Integer.parseInt(csvRecord.get(HEADERS[3])));
                candidate.setQuartersInLux(Integer.parseInt(csvRecord.get(HEADERS[4])));
                candidate.setProductions(new ArrayList<>(Arrays.asList(csvRecord.get(HEADERS[5]),
                                                            csvRecord.get(HEADERS[6]),
                                                            csvRecord.get(HEADERS[7]))));
                candidate.setRoles(new ArrayList<>(Arrays.asList(csvRecord.get(HEADERS[8]),
                                                    csvRecord.get(HEADERS[9]),
                                                    csvRecord.get(HEADERS[10]))));
                candidate.setProdPriority(csvRecord.get(HEADERS[11]).equals("Production"));
                candidate.setAssigned(false);

                // Add the candidate to the list
                candidates.add(candidate);
            }

            return candidates;

        } catch (IOException e) {
            throw new RuntimeException("failed to parse CSV file: " + e.getMessage());
        }
    }
}
