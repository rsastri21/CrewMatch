package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.repositories.ProductionRepository;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionRepository productionRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    MatchService matchService;

    // Dependency Injection
    public ProductionController(ProductionRepository productionRepository, CandidateRepository candidateRepository) {
        this.productionRepository = productionRepository;
        this.candidateRepository = candidateRepository;
    }

    // Get all productions
    @GetMapping("/get")
    public Iterable<Production> getAllProductions() {
        return this.productionRepository.findAll();
    }

    // Get production by id
    @GetMapping("/get/{id}")
    public Production getProductionById(@PathVariable("id") Integer id) {
        Optional<Production> productionOptional = this.productionRepository.findById(id);

        if (productionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }

        return productionOptional.get();
    }

    // Match crew to productions
    @GetMapping("/match")
    public ResponseEntity<String> matchCandidatesToProductions() {
        return matchService.match();
    }

    // Match crew to productions without absolute preference considerations
    @GetMapping("/matchNoPreference")
    public ResponseEntity<String> matchCandidatesToProductionsNoPreferences() {
        return matchService.matchWithoutPreference();
    }

    // Search for a production by name
    @GetMapping("/search")
    public Production searchProductionsByName(@RequestParam(name = "name") String name) {
        Optional<Production> productionOptional = Optional.ofNullable(this.productionRepository.findByName(name));
        if (productionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that name.");
        }

        return productionOptional.get();
    }

    // Create a new production
    @PostMapping("/create")
    public ResponseEntity<Production> createNewProduction(@RequestBody Production production) {
        // See if provided lists are the same length
        if (production.getMembers().size() != production.getRoles().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roles and Members lists must be the same length.");
        }
        // Prior to saving, check if members in the production are not created candidates yet
        List<String> crew = production.getMembers();
        for (String member : crew) {
            // See if candidate exists, create an entry if not
            Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(member));
            if (candidateOptional.isEmpty()) {
                Candidate candidateToAdd = new Candidate();
                candidateToAdd.setName(member);
                candidateToAdd.setAssigned(true);

                this.candidateRepository.save(candidateToAdd);
            } else {
                // If the candidate already exists, set assigned property to true
                Candidate candidateToUpdate = candidateOptional.get();
                candidateToUpdate.setAssigned(true);
                this.candidateRepository.save(candidateToUpdate);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.productionRepository.save(production));
    }

    // Update a production
    @PutMapping("/update/{id}")
    public Production updateProduction(@PathVariable("id") Integer id, @RequestBody Production p) {
        // Pull existing production from the repository
        Optional<Production> productionToUpdateOptional = this.productionRepository.findById(id);

        if (productionToUpdateOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }
        Production productionToUpdate = productionToUpdateOptional.get();

        // Check fields and update accordingly
        if (p.getName() != null) {
            productionToUpdate.setName(p.getName());
        }
        if (p.getRoles() != null) {
            productionToUpdate.setRoles(p.getRoles());
        }
        if (p.getMembers() != null) {
            productionToUpdate.setMembers(p.getMembers());
        }

        return this.productionRepository.save(productionToUpdate);
    }

    // Swap members between productions
    @PutMapping("/swap/{production1}/{member1}/{production2}/{member2}")
    public ResponseEntity<String> swapMembers(@PathVariable("production1") String production1,
                                              @PathVariable("member1") String member1,
                                              @PathVariable("production2") String production2,
                                              @PathVariable("member2") String member2) {
        // Find the first production
        Optional<Production> productionOptionalOne = Optional.ofNullable(this.productionRepository.findByName(production1));
        if (productionOptionalOne.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no production matching the first name.");
        }
        Production productionOne = productionOptionalOne.get();

        // Find the second production
        Optional<Production> productionOptionalTwo = Optional.ofNullable(this.productionRepository.findByName(production2));
        if (productionOptionalTwo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no production matching the second name.");
        }
        Production productionTwo = productionOptionalTwo.get();

        // Check members are not missing
        if (!checkMemberPresent(productionOne, member1) || !checkMemberPresent(productionTwo, member2)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One of the members is missing from its production.");
        }

        // Swap members
        swapMembers(productionOne, member1, member2);
        swapMembers(productionTwo, member2, member1);

        return ResponseEntity.status(HttpStatus.OK).body("The members have been swapped.");

    }


    // Check if a member is present in a given production
    private boolean checkMemberPresent(Production production, String memberToCheck) {
        for (String member : production.getMembers()) {
            if (member.equals(memberToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Helper method that places member 2 in place of member1 on a certain production
    // Performs only one swap
    private void swapMembers(Production production, String member1, String member2) {
        List<String> prodMembers = new ArrayList<>(production.getMembers());
        for (int i = 0; i < prodMembers.size(); i++) {
            if (member1.equals(prodMembers.get(i))) {
                prodMembers.set(i, member2);
                production.setMembers(prodMembers);
                this.productionRepository.save(production);
                break;
            }
        }
    }

    // Delete a production
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The production has been deleted.")
    public void deleteProduction(@PathVariable("id") Integer id) {
        Optional<Production> productionToDeleteOptional = this.productionRepository.findById(id);

        if (productionToDeleteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }
        Production productionToDelete = productionToDeleteOptional.get();

        this.productionRepository.delete(productionToDelete);
    }

    // Delete all productions
    // For internal use only -- Only publish API endpoint if behind two-step verification
    @DeleteMapping("/deleteAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All productions have been deleted.")
    public void deleteAllProductions() {
        this.productionRepository.deleteAll();
    }



}
