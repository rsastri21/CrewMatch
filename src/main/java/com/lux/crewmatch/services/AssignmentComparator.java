package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Assignment;

import java.util.Comparator;

public class AssignmentComparator implements Comparator<Assignment> {

    @Override
    public int compare(Assignment a1, Assignment a2) {
        int cmp;

        // Compare according to the weight of the assignment
        cmp = -Double.compare(a1.getWeight(), a2.getWeight());

        return cmp;
    }
}
