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
        int numCandidatesAssigned = 0;
        int numProductions;

        // Input processing:
        // - If there are no candidates or no productions, the matching algorithm should not commence.
        ResponseEntity<String> EXPECTATION_FAILED = getStringResponseEntity(candidateList);
        if (EXPECTATION_FAILED != null) return EXPECTATION_FAILED;
        numProductions = (int) this.productionRepository.count();

        PriorityQueue<Candidate> orderedCandidates = new PriorityQueue<>(new CandidateComparator());
        orderedCandidates.addAll(candidateList);

        // Iterate through candidate in sorted order
        while (!orderedCandidates.isEmpty()) {
            Candidate candidate = orderedCandidates.poll();

            if (candidate.getProdPriority()) {
                // True when candidate prefers to be placed on desired production over role.
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
                    for (String role : new ArrayList<>(candidate.getRoles())) {
                        if (productionToTry.place(candidate, role)) {
                            numCandidatesAssigned++;
                            candidate.setAssigned(true);
                            productionRepository.save(productionToTry);
                            candidateRepository.save(candidate);
                            break;
                        }
                    }
                }

            } else {
                // Branch where candidate prefers roles to productions.
                for (String role : new ArrayList<>(candidate.getRoles())) {
                    // Exit loop if candidate becomes assigned
                    if (candidate.getAssigned()) {
                        break;
                    }

                    for (String production : new ArrayList<>(candidate.getProductions())) {
                        Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(production));
                        if (productionOptional.isEmpty()) {
                            continue;
                        }

                        Production productionToTry = productionOptional.get();
                        // Attempt to place with the given role
                        if (productionToTry.place(candidate, role)) {
                            numCandidatesAssigned++;
                            candidate.setAssigned(true);
                            productionRepository.save(productionToTry);
                            candidateRepository.save(candidate);
                            break;
                        }
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                numCandidatesAssigned + " have been placed on " + numProductions + " productions."
        );

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

}
