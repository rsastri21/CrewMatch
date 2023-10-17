package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.repositories.ProductionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class MatchService {

    // Repositories
    @Autowired
    CandidateRepository candidateRepository;

    @Autowired
    ProductionRepository productionRepository;
    
    // Method to match candidates to productions
    // Returns an http response stating how many candidates were matched
    public ResponseEntity<String> match() {
        // Only assigning unassigned candidates
        List<Candidate> candidateList = candidateRepository.findByAssignedFalseAndActingInterestFalse();
        int totalCandidates = candidateList.size();
        int numCandidatesAssigned = 0;
        int numProductions;

        // Input processing:
        // - If there are no candidates or no productions, the matching algorithm should not commence.
        ResponseEntity<String> EXPECTATION_FAILED = getStringResponseEntity(candidateList);
        if (EXPECTATION_FAILED != null) return EXPECTATION_FAILED;
        numProductions = (int) this.productionRepository.count();

        PriorityQueue<Candidate> orderedCandidates = new PriorityQueue<>(new CandidateComparator());
        for (Candidate candidate : candidateList) {
            if (candidate.isComplete()) {
                orderedCandidates.add(candidate);
            }
        }

        // Iterate through candidate in sorted order
        while (!orderedCandidates.isEmpty()) {
            Candidate candidate = orderedCandidates.poll();

            // Skip the candidate if it does not contain all required fields to match
            if (!candidate.isComplete()) {
                continue;
            }

            if (candidate.getProdPriority()) {
                // True when candidate prefers to be placed on desired production over role.
                numCandidatesAssigned += assignToProductionWithProductionBias(candidate, new ArrayList<>(candidate.getRoles()), false);

            } else {
                // Branch where candidate prefers roles to productions.
                numCandidatesAssigned += assignToProductionWithRoleBias(candidate, new ArrayList<>(candidate.getProductions()), false);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                numCandidatesAssigned + " have been placed on " + numProductions + " productions. " + (totalCandidates - numCandidatesAssigned) +
                " remain to be matched."
        );

    }

    // Method to match candidates without taking production/role preferences into consideration
    // Returns an http response with how many candidates were matched.
    public ResponseEntity<String> matchWithoutPreference() {
        // Select unassigned candidates
        List<Candidate> candidateList = this.candidateRepository.findByAssignedFalseAndActingInterestFalse();
        int totalCandidates = candidateList.size();
        int numCandidatesAssigned = 0;
        int numProductions;

        // Input processing
        ResponseEntity<String> EXPECTATION_FAILED = getStringResponseEntity(candidateList);
        if (EXPECTATION_FAILED != null) return EXPECTATION_FAILED;
        numProductions = (int) this.productionRepository.count();

        // Initialize ordered candidate list
        PriorityQueue<Candidate> orderedCandidates = new PriorityQueue<>(new CandidateComparator());
        for (Candidate candidate : candidateList) {
            if (candidate.isComplete()) {
                orderedCandidates.add(candidate);
            }
        }

        while (!orderedCandidates.isEmpty()) {
            Candidate candidate = orderedCandidates.poll();

            // Skip the candidate if it does not contain all required fields to match
            if (!candidate.isComplete()) {
                continue;
            }

            // Based on production priority, assign candidate to any role in their top productions
            // or any production with their top roles
            if (candidate.getProdPriority()) {
                // The roles to choose from will be determined by the roles in stored productions
                numCandidatesAssigned += assignToProductionWithProductionBias(candidate, null, true);
            } else {
                // The productions to choose from will be determined by all available ones in the repository
                numCandidatesAssigned += assignToProductionWithRoleBias(candidate, null, true);
            }
        }

        // If there are still candidates to assign, do so without any preferences considered
        // Last resort --> Nothing available that fits the candidates' choices
        // Start by rechecking for candidates that are unmatched
        candidateList = new ArrayList<>(this.candidateRepository.findByAssignedFalseAndActingInterestFalse());
        for (Candidate candidate : candidateList) {
            if (candidate.isComplete()) {
                orderedCandidates.add(candidate);
            }
        }

        while (!orderedCandidates.isEmpty()) {
            Candidate candidate = orderedCandidates.poll();

            // Obtain all productions
            List<String> productions = obtainAllProductionNames();
            for (String production : productions) {

                Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(production));
                if (productionOptional.isEmpty()) {
                    continue;
                }
                Production productionToTry = productionOptional.get();

                // Try to place candidate in any role
                for (String role : new ArrayList<>(productionToTry.getRoles())) {
                    if (productionToTry.place(candidate, role)) {
                        numCandidatesAssigned++;
                        candidate.assign(productionToTry, role);
                        productionRepository.save(productionToTry);
                        candidateRepository.save(candidate);
                        break;
                    }
                }
            }

        }

        return ResponseEntity.status(HttpStatus.OK).body(
                numCandidatesAssigned + " have been placed on " + numProductions + " productions. " + (totalCandidates - numCandidatesAssigned) +
                        " remain to be matched."
        );

    }

    private Integer assignToProductionWithProductionBias(Candidate candidate, List<String> roles, boolean useProdRoles) {
        int numCandidatesAssigned = 0;

        for (String production : new ArrayList<>(candidate.getProductions())) {
            // Exit loop if candidate becomes assigned
            if (candidate.getAssigned()) {
                break;
            }

            Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(production));
            if (productionOptional.isEmpty()) {
                continue;
            }

            // Try to place a candidate on a production with any roles.
            Production productionToTry = productionOptional.get();
            List<String> rolesToUse = useProdRoles ? new ArrayList<>(productionToTry.getRoles()) : roles;
            for (String role : rolesToUse) {
                if (productionToTry.place(candidate, role)) {
                    numCandidatesAssigned++;
                    candidate.assign(productionToTry, role);
                    productionRepository.save(productionToTry);
                    candidateRepository.save(candidate);
                    break;
                }
            }
        }

        return numCandidatesAssigned;
    }

    private Integer assignToProductionWithRoleBias(Candidate candidate, List<String> productions, boolean useRepoProds) {
        int numCandidatesAssigned = 0;

        for (String role : new ArrayList<>(candidate.getRoles())) {
            // Exit loop if candidate becomes assigned
            if (candidate.getAssigned()) {
                break;
            }

            // Use appropriate productions names
            List<String> prodsToUse = useRepoProds ? obtainAllProductionNames() : productions;

            for (String production : prodsToUse) {
                Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(production));
                if (productionOptional.isEmpty()) {
                    continue;
                }

                Production productionToTry = productionOptional.get();
                // Attempt to place with the given role
                if (productionToTry.place(candidate, role)) {
                    numCandidatesAssigned++;
                    candidate.assign(productionToTry, role);
                    productionRepository.save(productionToTry);
                    candidateRepository.save(candidate);
                    break;
                }
            }
        }

        return numCandidatesAssigned;
    }

    // Helper method that determines if there are candidate and productions to be matched.
    private ResponseEntity<String> getStringResponseEntity(List<Candidate> candidateList) {
        if (candidateList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("There are no candidates to match.");
        }
        if ((int) this.productionRepository.count() == 0) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("No productions have been created.");
        }
        return null;
    }

    private List<String> obtainAllProductionNames() {
        List<Production> allProductions = productionRepository.findByArchived(false);
        List<String> prodNames = new ArrayList<>();
        for (Production prod : allProductions) {
            prodNames.add(prod.getName());
        }

        return prodNames;
    }

}
