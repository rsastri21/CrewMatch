package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.services.CSVHelper;
import com.lux.crewmatch.message.ResponseMessage;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.services.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateRepository candidateRepository;

    @Autowired
    CSVService fileService;

    // Dependency Injection
    public CandidateController(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    // Get all candidates
    @GetMapping("/get")
    public Iterable<Candidate> getAllCandidates() {
        return this.candidateRepository.findAll();
    }

    // Get a candidate by ID
    @GetMapping("/get/{id}")
    public Candidate getCandidateById(@PathVariable("id") Integer id) {
        Optional<Candidate> candidate = this.candidateRepository.findById(id);

        if (candidate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate matching that ID.");
        }

        return candidate.get();
    }

    // Get candidates by search
    @GetMapping("/search")
    public List<Candidate> searchCandidates(@RequestParam(name = "assigned", required = false) Boolean assigned) {
        if (assigned != null && !assigned) {
            return this.candidateRepository.findByAssignedFalse();
        }
        return new ArrayList<>();
    }

    // Create a new candidate
    @PostMapping("/add")
    public Candidate createNewCandidate(@RequestBody Candidate candidate) {
        return this.candidateRepository.save(candidate);
    }

    // Create new candidates from CSV
    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.isValidFile(file)) {
            try {
                fileService.save(file);

                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }

        message = "Please upload a CSV file.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));

    }


    // Update a candidate
    @PutMapping("/update/{id}")
    public Candidate updateCandidate(@PathVariable("id") Integer id, @RequestBody Candidate c) {
        // Get the candidate from the repository.
        Optional<Candidate> candidateToUpdateOptional = this.candidateRepository.findById(id);

        if (candidateToUpdateOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate matching that ID.");
        }

        Candidate candidateToUpdate = candidateToUpdateOptional.get();

        // Check fields and update accordingly.
        if (c.getName() != null) {
            candidateToUpdate.setName(c.getName());
        }
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

        return this.candidateRepository.save(candidateToUpdate);

    }

    // Delete a candidate
    @DeleteMapping("/delete/{id}")
    public Candidate deleteCandidate(@PathVariable("id") Integer id) {
        Optional<Candidate> candidateToDeleteOptional = this.candidateRepository.findById(id);

        if (candidateToDeleteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate with that ID.");
        }

        Candidate candidateToDelete = candidateToDeleteOptional.get();
        this.candidateRepository.delete(candidateToDelete);
        return candidateToDelete;
    }

}
