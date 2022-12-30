package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Candidate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class CandidateComparator implements Comparator<Candidate> {

    @Override
    public int compare(Candidate o1, Candidate o2) {
        int cmp;

        // First, compare according to years in UW
        cmp = -Integer.compare(o1.getYearsInUW(), o2.getYearsInUW());
        if (cmp != 0) {
            return cmp;
        }

        // Second, compare according to quarters in LUX
        cmp = -Integer.compare(o1.getQuartersInLux(), o2.getQuartersInLux());
        if (cmp != 0) {
            return cmp;
        }

        // Third, compare according to timestamp
        try {
            cmp = compareDate(o1.getTimestamp(), o2.getTimestamp());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (cmp != 0) {
            return cmp;
        }

        // Finally, compare according to name if all else is equivalent (unlikely)
        cmp = o1.getName().compareTo(o2.getName());

        return cmp;
    }

    // Helper method to parse timestamps and return an integer comparison.
    private int compareDate(String timestamp1, String timestamp2) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy H:mm:ss");
        Date date1 = dateFormat.parse(timestamp1);
        Date date2 = dateFormat.parse(timestamp2);

        return date1.compareTo(date2);
    }
}
