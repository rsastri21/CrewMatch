package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Assignment;
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
public class WeightedMatchService {

    // Repository configuration
    @Autowired
    CandidateRepository candidateRepository;

    @Autowired
    ProductionRepository productionRepository;

    // Candidate weight constant
    private static final Double[] CANDIDATE_WEIGHTS = { 0.5, 0.35, 0.15 };

    // Method to match candidates to productions with production role weights considered
    // Returns an HTTP response stating how many candidates were matched
    public ResponseEntity<String> weightedMatch() {
        // Get all unassigned candidates
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
        orderedCandidates.addAll(candidateList);

        // Iterate candidates in sorted order
        while (!orderedCandidates.isEmpty()) {
            Candidate candidate = orderedCandidates.poll();

            if (candidate.getProdPriority()) {
                numCandidatesAssigned += assignToProductionWithWeightedProductionBias(candidate, new ArrayList<>(candidate.getRoles()));
            } else {
                numCandidatesAssigned += assignToProductionWithWeightedRoleBias(candidate, new ArrayList<>(candidate.getProductions()));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                numCandidatesAssigned + " have been placed on " + numProductions + " productions. " + (totalCandidates - numCandidatesAssigned) +
                        " remain to be matched."
        );
    }

    private Integer assignToProductionWithWeightedProductionBias(Candidate candidate, List<String> roles) {
        int numCandidatesAssigned = 0;

        for (int i = 0; i < candidate.getProductions().size(); i++) {
            // Exit the loop if candidate becomes assigned
            if (candidate.getAssigned()) {
                break;
            }

            String production = candidate.getProductions().get(i);

            Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(production));
            if (productionOptional.isEmpty()) {
                continue;
            }

            // Priority Queue to store potential assignments on a given production
            PriorityQueue<Assignment> potentialAssignments = new PriorityQueue<>(new AssignmentComparator());

            // Try to place a candidate on the production
            Production productionToTry = productionOptional.get();
            List<String> prodRoles = productionToTry.getRoles();
            List<Double> roleWeights = new ArrayList<>(productionToTry.getRoleWeights());

            for (int j = 0; j < roles.size(); j++) {
                // Compute the weight of the potential assignment
                for (int k = 0; k < prodRoles.size(); k++) {
                    // Skip if not currently selecting the same role from the production and candidate
                    if (!prodRoles.get(k).equals(roles.get(j))) {
                        continue;
                    }

                    double weight = roleWeights.get(k) * CANDIDATE_WEIGHTS[j];
                    // Save the potential assignment in the queue
                    Assignment assignment = new Assignment(candidate, productionToTry, k, prodRoles.get(k), weight);
                    potentialAssignments.add(assignment);
                }
            }

            // Attempt placement based on ranking in the priority queue
            while (!potentialAssignments.isEmpty()) {
                Assignment assignment = potentialAssignments.poll();
                String role = assignment.getRole();
                if (productionToTry.place(candidate, role)) {
                    numCandidatesAssigned++;
                    // Update candidate
                    candidate.setAssigned(true);
                    candidate.setRole(role);
                    candidate.setProduction(productionToTry.getName());
                    candidateRepository.save(candidate);

                    // Update production
                    roleWeights.set(assignment.getAssignmentIndex(), 0.0);
                    productionToTry.setRoleWeights(roleWeights);
                    productionRepository.save(productionToTry);

                    break;

                }
            }

        }

        return numCandidatesAssigned;
    }

    private Integer assignToProductionWithWeightedRoleBias(Candidate candidate, List<String> productions) {
        int numCandidatesAssigned = 0;

        for (int i = 0; i < candidate.getRoles().size(); i++) {
            // Exit loop if candidate becomes assigned
            if (candidate.getAssigned()) {
                break;
            }

            String role = candidate.getRoles().get(i);

            // Priority Queue to store potential assignments on a given production
            PriorityQueue<Assignment> potentialAssignments = new PriorityQueue<>(new AssignmentComparator());

            // Iterate through candidate's production rankings
            for (int j = 0; j < productions.size(); j++) {
                // Check that production exists
                Optional<Production> productionOptional = Optional.ofNullable(productionRepository.findByName(productions.get(j)));
                if (productionOptional.isEmpty()) {
                    continue;
                }

                Production production = productionOptional.get();
                // Compute the weights of the role in each production
                List<String> prodRoles = production.getRoles();
                List<Double> roleWeights = new ArrayList<>(production.getRoleWeights());
                for (int k = 0; k < prodRoles.size(); k++) {
                    if (!prodRoles.get(k).equals(role)) {
                        continue;
                    }

                    double weight = roleWeights.get(k) * CANDIDATE_WEIGHTS[j];
                    // Save the weight as a potential assignment
                    Assignment potentialAssignment = new Assignment(candidate, production, k, role, weight);
                    potentialAssignments.add(potentialAssignment);
                }
            }

            // Find the optimal assignment
            while (!potentialAssignments.isEmpty()) {
                Assignment assignment = potentialAssignments.poll();
                String roleToTry = assignment.getRole();
                Production productionToTry = assignment.getProduction();
                List<Double> roleWeights = new ArrayList<>(productionToTry.getRoleWeights());
                if (productionToTry.place(candidate, roleToTry)) {
                    numCandidatesAssigned++;
                    // Update candidate
                    candidate.setAssigned(true);
                    candidate.setRole(role);
                    candidate.setProduction(productionToTry.getName());
                    candidateRepository.save(candidate);

                    // Update production
                    roleWeights.set(assignment.getAssignmentIndex(), 0.0);
                    productionToTry.setRoleWeights(roleWeights);
                    productionRepository.save(productionToTry);

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

}
