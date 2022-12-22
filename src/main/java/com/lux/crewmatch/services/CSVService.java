package com.lux.crewmatch.services;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.repositories.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class CSVService {

    @Autowired
    CandidateRepository candidateRepository;

    public void save(MultipartFile file) {
        try {
            List<Candidate> candidates = CSVHelper.csvToCandidates(file.getInputStream());
            candidateRepository.saveAll(candidates);
        } catch (IOException e) {
            throw new RuntimeException("failed to store csv data: " + e.getMessage());
        }
    }


}
