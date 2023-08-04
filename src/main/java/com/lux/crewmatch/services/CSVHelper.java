package com.lux.crewmatch.services;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.lux.crewmatch.entities.Production;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.lux.crewmatch.entities.Candidate;

public class CSVHelper {

    public static String TYPE = "text/csv";

    /*
     *  Checks the validity of an uploaded file.
     *  Params:
     *      MultipartFile file - a csv file to be checked.
     *  Returns:
     *      A boolean indicating whether the uploaded file is a csv.
     */
    public static boolean isValidFile(MultipartFile file) {

        return TYPE.equals(file.getContentType());

    }

    /**
     * Helper method to format names with capital first letters of each word.
     * @param name - The name to be formatted.
     * @return _ Returns a formatted version of the name.
     */
    public static String formatName(String name) {

        // Input processing
        if (name.length() == 0) {
            return name;
        }

        String[] names = name.split("\\s+");

        StringBuilder result = new StringBuilder();
        // Capitalize first letter
        for (String entry : names) {
            String formattedName = Character.toUpperCase(entry.charAt(0)) + entry.substring(1);
            result.append(formattedName).append(" ");
        }

        return result.toString().trim();
    }

    public static List<Candidate> csvToCandidates(InputStream inputStream, String[] HEADERS) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<Candidate> candidates = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                try {
                    // Create a candidate with the fields from the csv
                    Candidate candidate = new Candidate();
                    if (!csvRecord.get(HEADERS[0]).equals("")) {
                        candidate.setName(formatName(csvRecord.get(HEADERS[0])));
                    }
                    // Previous forms did not have this field, so it is wrapped in an if-statement.
                    if (!HEADERS[1].equals("")) {
                        candidate.setPronouns(csvRecord.get(HEADERS[1]));
                    }
                    candidate.setEmail(csvRecord.get(HEADERS[2]));
                    candidate.setTimestamp(csvRecord.get(HEADERS[3]));
                    candidate.setYearsInUW(Integer.parseInt(csvRecord.get(HEADERS[4])));
                    candidate.setQuartersInLux(Integer.parseInt(csvRecord.get(HEADERS[5])));
                    // Logic for a candidate indicating a preference for acting
                    if (csvRecord.get(HEADERS[13]).equals("Yes")) {
                        candidate.setActingInterest(true);
                        List<String> actingProductions = new ArrayList<>(Arrays.asList(csvRecord.get(HEADERS[14]).split(",")));
                        candidate.setProductions(actingProductions);
                    } else {
                        candidate.setActingInterest(false);
                        candidate.setProductions(new ArrayList<>(Arrays.asList(csvRecord.get(HEADERS[6]),
                                csvRecord.get(HEADERS[7]),
                                csvRecord.get(HEADERS[8]))));
                        candidate.setRoles(new ArrayList<>(Arrays.asList(csvRecord.get(HEADERS[9]),
                                csvRecord.get(HEADERS[10]),
                                csvRecord.get(HEADERS[11]))));
                        candidate.setProdPriority(csvRecord.get(HEADERS[12]).equals("Production"));
                    }
                    candidate.setAssigned(false);

                    // Add the candidate to the list
                    candidates.add(candidate);
                } catch (Exception e) {
                    throw new RuntimeException("The csv headers do not match. Please update them before processing" +
                            " candidates.");
                }
            }

            return candidates;

        } catch (IOException e) {
            throw new RuntimeException("failed to parse CSV file: " + e.getMessage());
        }
    }

    // Convert all the productions data to a 2d array which can be outputted to CSV.
    public static List<String[]> dataToGrid(List<Production> productions) {
        // Initialize output
        List<String[]> output = new ArrayList<>();

        // Prepare width dimension of output
        int width = 3 * productions.size() - 1;

        // Get number of rows required for each production
        List<Integer> productionLengths = getProductionLengths(productions);
        int maxLength = Collections.max(productionLengths);

        // Beginning row containing production names
        String[] productionNames = new String[width];
        String[] productionHeaders = new String[width];
        Arrays.fill(productionNames, "");
        Arrays.fill(productionHeaders, "");

        for (int i = 0; i < productions.size(); i++) {
            if (productions.get(i).getArchived()) {
                productionNames[3 * i] = productions.get(i).getName() + " (Archived)";
            } else {
                productionNames[3 * i] = productions.get(i).getName();
            }
            productionHeaders[3 * i] = "Role";
            productionHeaders[3 * i + 1] = "Member";
        }
        output.add(productionNames);
        output.add(productionHeaders);

        // Create other rows
        for (int i = 0; i < maxLength; i++) {
            String[] row = new String[width];
            Arrays.fill(row, "");
            for (int j = 0; j < productions.size(); j++) {
                Production production = productions.get(j);
                if (i < productionLengths.get(j)) {
                    row[3 * j] = production.getRoles().get(i);
                    row[3 * j + 1] = production.getMembers().get(i);
                }
            }
            output.add(row);
        }

        return output;
    }

    // Get the number of rows each production will require
    private static List<Integer> getProductionLengths(List<Production> productions) {
        List<Integer> prodLengths = new ArrayList<>();

        for (Production productionToCheck : productions) {
            // Add the length of the roles list
            prodLengths.add(productionToCheck.getRoles().size());
        }

        return prodLengths;
    }
}
