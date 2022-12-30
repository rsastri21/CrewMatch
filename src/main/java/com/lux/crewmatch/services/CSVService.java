package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.repositories.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CSVService {

    @Autowired
    CandidateRepository candidateRepository;

    public void save(MultipartFile file) {
        try {
            List<Candidate> candidates = CSVHelper.csvToCandidates(file.getInputStream());

            // Check if a candidate already exists, and if it does --> Update it
            for (Candidate c : candidates) {
                Optional<Candidate> candidateToUpdateOptional = Optional.ofNullable(candidateRepository.findByName(c.getName()));

                if (candidateToUpdateOptional.isEmpty()) {
                    // Save the candidate as-is if it does not exist yet
                    candidateRepository.save(c);
                } else {
                    Candidate candidateToUpdate = candidateToUpdateOptional.get();

                    // Update fields accordingly
                    // As candidate was found by name, that parameter is not updated.
                    updateCandidate(c, candidateToUpdate);
                    candidateRepository.save(candidateToUpdate);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to store csv data: " + e.getMessage());
        }
    }


    // A helper method which updates all parameters of a candidate aside from name and id.
    public static void updateCandidate(Candidate c, Candidate candidateToUpdate) {
        if (c.getPronouns() != null) {
            candidateToUpdate.setPronouns(c.getPronouns());
        }
        if (c.getEmail() != null) {
            candidateToUpdate.setEmail(c.getEmail());
        }
        if (c.getTimestamp() != null) {
            candidateToUpdate.setTimestamp(c.getTimestamp());
        }
        if (c.getYearsInUW() != null) {
            candidateToUpdate.setYearsInUW(c.getYearsInUW());
        }
        if (c.getQuartersInLux() != null) {
            candidateToUpdate.setQuartersInLux(c.getQuartersInLux());
        }
        if (c.getActingInterest() != null) {
            candidateToUpdate.setActingInterest(c.getActingInterest());
        }
        if (c.getProductions() != null) {
            candidateToUpdate.setProductions(c.getProductions());
        }
        if (c.getRoles() != null) {
            candidateToUpdate.setRoles(c.getRoles());
        }
        if (c.getProdPriority() != null) {
            candidateToUpdate.setProdPriority(c.getProdPriority());
        }
        if (c.getAssigned() != null) {
            candidateToUpdate.setAssigned(c.getAssigned());
        }
    }


}
