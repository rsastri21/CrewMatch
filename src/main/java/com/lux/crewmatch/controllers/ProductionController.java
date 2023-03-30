package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.entities.SwapRequest;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.repositories.ProductionRepository;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.repositories.SwapRequestRepository;
import com.lux.crewmatch.services.CSVService;
import com.lux.crewmatch.services.MatchService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.InputStreamResource;
import com.lux.crewmatch.services.CSVHelper;

import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionRepository productionRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    MatchService matchService;

    @Autowired
    CSVService fileService;

    /**
     * Creates an instance of the production controller to handle requests relating to productions.
     * The purpose of this constructor is to configure the appropriate dependency injection for Spring Boot.
     * @param productionRepository - The production repository where production entities are stored.
     * @param candidateRepository - The candidate repository where candidate entities are stored.
     */
    public ProductionController(ProductionRepository productionRepository, CandidateRepository candidateRepository) {
        this.productionRepository = productionRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Gets all productions stored in the production repository.
     * Accepts HTTP GET requests at the "./get" API endpoint.
     * @return - Returns an iterable containing all the productions currently stored in the production repository.
     */
    @GetMapping("/get")
    public Iterable<Production> getAllProductions() {
        return this.productionRepository.findAll();
    }

    /**
     * Gets all productions that do not have an assigned production lead.
     * Accepts HTTP GET requests at the "./getNoLead" API endpoint.
     * @return - returns a list containing all the productions that match the criteria.
     */
    @GetMapping("/getNoLead")
    public List<Production> getProductionsNoLead() {
        return this.productionRepository.findByProdLeadIsNull();
    }

    /**
     * Gets all productions that have an assigned production lead.
     * Accepts HTTP GET requests at the "./getLead" API endpoint.
     * @return - returns a list containing all the productions that match the criteria.
     */
    @GetMapping("/getLead")
    public List<Production> getProductionsWithLead() {
        return this.productionRepository.findByProdLeadIsNotNull();
    }

    /**
     * Gets the number of productions currently stored in the repository at the "./getCount" API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns a ResponseEntity with an OK status code. The body of the response
     * contains the integer count of productions.
     */
    @GetMapping("/getCount")
    public ResponseEntity<Integer> getNumberOfProductions() {
        return ResponseEntity.status(HttpStatus.OK).body((int) this.productionRepository.count());
    }

    /**
     * Gets a production by ID. Throws a bad request exception if there is not a production matching the id.
     * Accepts HTTP GET requests at the "./get/{id}" API endpoint.
     * @param id - An integer identifying the production to be retrieved entered as a path variable.
     * @return - Returns a production that matches the ID.
     */
    @GetMapping("/get/{id}")
    public Production getProductionById(@PathVariable("id") Integer id) {
        Optional<Production> productionOptional = this.productionRepository.findById(id);

        if (productionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }

        return productionOptional.get();
    }

    /**
     * Gets all the roles that contained within any production.
     * Intended for use prior to creating the Role Interest Form, allowing all candidates
     * to select roles that are contained within a production's needs.
     * Accepts HTTP GET requests at the "./get/roles" API endpoint.
     * @return - Returns a list of strings containing all the roles contained on all the productions.
     */
    @GetMapping("/get/roles")
    public List<String> getAllRoles() {
        Iterable<Production> productions = this.productionRepository.findAll();

        Set<String> roles = new HashSet<>();
        // Iterate through every production and add unique roles
        for (Production production : productions) {
            roles.addAll(production.getRoles());
        }

        return roles.stream().toList();
    }

    /**
     * Exports the data of all production assignments to CSV format.
     * Accepts HTTP GET requests at the "./getCSV/{filename}" API endpoint.
     * @param filename - A string path variable describing the name of the output file.
     * @return - Returns a CSV file with the assignment data.
     */
    @GetMapping("/getCSV/{filename}")
    public ResponseEntity<Resource> convertToCSV(@PathVariable("filename") String filename) {
        // Check that productions exist
        if (this.productionRepository.count() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are no productions to display.");
        }

        InputStreamResource fileInputStream = fileService.dataToCSV(this.productionRepository.findAll());

        String csvFileName = filename + ".csv";

        // HTTP Headers for response
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
        // Specifying return type
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

        return new ResponseEntity<>(
                fileInputStream,
                headers,
                HttpStatus.OK
        );

    }

    /**
     * Matches the candidates in the candidate repository to productions using MatchService.match().
     * Accepts HTTP GET requests at the "./match" API endpoint.
     * @return - Returns a ResponseEntity with a message of the format "X candidates have been matched to X productions."
     * if the request was successful, or an error if there are no candidates or no productions.
     */
    @GetMapping("/match")
    public ResponseEntity<String> matchCandidatesToProductions() {
        return matchService.match();
    }

    /**
     * Matches candidates to productions using the MatchService.matchWithoutPreference() method. Does not strictly take
     * into account candidate preferences.
     * Accepts HTTP GET requests at "./matchNoPreference" API endpoint.
     * Primarily used for matching candidates whose preferences do not align with available spaces.
     * @return - Returns a ResponseEntity with a message of the format "X candidates have been matched to X productions."
     * if the request was successful, or an error if there are no candidates or no productions.
     */
    @GetMapping("/matchNoPreference")
    public ResponseEntity<String> matchCandidatesToProductionsNoPreferences() {
        return matchService.matchWithoutPreference();
    }

    /**
     * Searches for productions by name. Throws a bad request exception if no production matches the name entered.
     * Accepts HTTP GET requests at the "./search" API endpoint.
     * @param name - A string entered as a query parameter that specifies the production to find.
     * @return - Returns the production from the repository that matches the specified name.
     */
    @GetMapping("/search")
    public Production searchProductionsByName(@RequestParam(name = "name") String name) {
        Optional<Production> productionOptional = Optional.ofNullable(this.productionRepository.findByName(name));
        if (productionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that name.");
        }

        return productionOptional.get();
    }

    /**
     * Creates a production according to parameters specified in the request body. Throws a bad request exception
     * if the provided lists defining roles and members in a production are different lengths.
     * Also creates dummy candidate instances if the candidates specified as members are not already in the candidate
     * repository. Marks all candidates involved in the production as assigned.
     * Accepts HTTP POST requests at the "./create" API endpoint.
     * @param production - The production body containing parameters to define the new instance.
     * @return - Returns a ResponseEntity with an OK response and the saved production instance in the body.
     */
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
            String memberName = CSVHelper.formatName(member);
            Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(memberName));
            if (candidateOptional.isEmpty()) {
                Candidate candidateToAdd = new Candidate();
                if (memberName.equals("")) {
                    continue;
                }
                candidateToAdd.setName(memberName);
                candidateToAdd.setAssigned(true);
                candidateToAdd.setActingInterest(false);

                this.candidateRepository.save(candidateToAdd);
            } else {
                // If the candidate already exists, set assigned property to true
                Candidate candidateToUpdate = candidateOptional.get();
                candidateToUpdate.setAssigned(true);
                this.candidateRepository.save(candidateToUpdate);
            }
        }

        // Capitalize all crew member names
        List<String> crewFormatted = new ArrayList<>(crew);
        for (int i = 0; i < crew.size(); i++) {
            crewFormatted.set(i, CSVHelper.formatName(crew.get(i)));
        }
        production.setMembers(new ArrayList<>(crewFormatted));

        return ResponseEntity.status(HttpStatus.OK).body(this.productionRepository.save(production));
    }

    /**
     * Updates a production according to parameters specified in the request body. Throws a bad request exception if
     * there is no production matching the id specified.
     * Accepts HTTP PUT requests at the "./update/{id}" API endpoint.
     * @param id - An integer that identifies which production is to be updated. Provided as a path variable in the request.
     * @param p - A serialized production instance containing the parameters to update the existing production.
     * @return - Returns the updated production.
     */
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

            List<String> members = new ArrayList<>(p.getMembers());

            // Iterate through candidates to validate with existing store
            for (int i = 0; i < members.size(); i++) {
                String member = members.get(i);
                // Pull the candidate from repository
                // Skip if member is ""
                if (member.equals("")) {
                    if (i >= productionToUpdate.getMembers().size()) {
                        continue;
                    }
                    // If previous value was not empty, set that candidate to unassigned
                    if (member.equals(productionToUpdate.getMembers().get(i))) {
                        continue;
                    }
                    Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(productionToUpdate.getMembers().get(i)));
                    if (candidateOptional.isEmpty()) {
                        continue;
                    }
                    Candidate candidateToUnassign = candidateOptional.get();
                    candidateToUnassign.setAssigned(false);
                    this.candidateRepository.save(candidateToUnassign);
                    continue;
                }
                Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(member));
                if (candidateOptional.isPresent()) {
                    // Set candidate assigned field to true
                    Candidate candidate = candidateOptional.get();
                    candidate.setAssigned(true);
                    this.candidateRepository.save(candidate);
                } else {
                    // Candidate not present -> Create a new skeleton candidate
                    Candidate newCandidate = new Candidate();
                    newCandidate.setName(member);
                    newCandidate.setAssigned(true);
                    newCandidate.setActingInterest(false);

                    // Save
                    this.candidateRepository.save(newCandidate);
                }
            }

            productionToUpdate.setMembers(members);

        }

        return this.productionRepository.save(productionToUpdate);
    }

    /**
     * Assigns a given candidate to a production in a specified role.
     * Accepts HTTP PUT requests at the "./assign/{productionID}/{candidateID}/{roleIndex}" API Endpoint.
     * Throws bad request or expectation failed responses if the production, candidate are not found, or if the role is
     * already occupied.
     * @param productionID - The ID of the production to which the candidate will be assigned. Provided as a path variable.
     * @param candidateID - The ID of the candidate to be assigned. Provided as a path variable.
     * @param roleIndex - The index of the desired role in the productions roles list. Provided as a path variable.
     * @return - Returns a response entity detailing the status of the assignment.
     */
    @PutMapping("/assign/{productionID}/{candidateID}/{roleIndex}")
    public ResponseEntity<String> manualCandidateAssign(@PathVariable("productionID") Integer productionID,
                                                        @PathVariable("candidateID") Integer candidateID,
                                                        @PathVariable("roleIndex") Integer roleIndex) {
        // Get the requested production
        Optional<Production> productionOptional = this.productionRepository.findById(productionID);
        if (productionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no production with that ID.");
        }
        Production productionToUpdate = productionOptional.get();

        // Check if the candidate is present
        Optional<Candidate> candidateOptional = this.candidateRepository.findById(candidateID);
        if (candidateOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no candidate with that ID.");
        }
        Candidate candidateToAssign = candidateOptional.get();

        // Check if candidate is already assigned
        if (candidateToAssign.getAssigned()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The candidate is already assigned.");
        }

        // Configure candidate name
        StringBuilder candidateDisplayName = new StringBuilder();
        candidateDisplayName.append(candidateToAssign.getName());
        if (candidateToAssign.getPronouns() != null) {
            candidateDisplayName.append(" (").append(candidateToAssign.getPronouns()).append(")");
        }

        // Assign the candidate to the requested role if it is empty
        List<String> productionMembers = new ArrayList<>(productionToUpdate.getMembers());
        if (!productionMembers.get(roleIndex).equals("")) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The desired role is already filled");
        }
        productionMembers.set(roleIndex, candidateDisplayName.toString());

        // Update production
        productionToUpdate.setMembers(productionMembers);
        this.productionRepository.save(productionToUpdate);

        // Change candidate status to assigned
        candidateToAssign.setAssigned(true);
        this.candidateRepository.save(candidateToAssign);

        return ResponseEntity.status(HttpStatus.OK).body("The candidate was assigned.");
    }

    /**
     * Removes a member from a production by unassigning them.
     * Accepts HTTP PUT requests at the "./unassign/{productionID}/{candidateID}/{roleIndex}" API endpoint.
     * @param productionID - An integer identifying the production to be updated.
     * @param candidateID - An integer identifying the candidate to be removed.
     * @param roleIndex - The index identifying the role of the candidate to be removed.
     * @return - Returns a Response Entity with a message containing the status of the removal.
     */
    @PutMapping("/unassign/{productionID}/{candidateID}/{roleIndex}")
    public ResponseEntity<String> manualCandidateRemoval(@PathVariable("productionID") Integer productionID,
                                                         @PathVariable("candidateID") Integer candidateID,
                                                         @PathVariable("roleIndex") Integer roleIndex) {
        // Get the requested production
        Optional<Production> productionOptional = this.productionRepository.findById(productionID);
        if (productionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no production with that ID.");
        }
        Production production = productionOptional.get();

        // Get the candidate
        Optional<Candidate> candidateOptional = this.candidateRepository.findById(candidateID);
        if (candidateOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no candidate with that ID.");
        }
        Candidate candidate = candidateOptional.get();

        // Iterate and find the candidate to remove
        List<String> crewMembers = new ArrayList<>(production.getMembers());

        for (int i = 0; i < production.getMembers().size(); i++) {
            if (production.getMembers().get(i).startsWith(candidate.getName()) && i == roleIndex) {
                candidate.setAssigned(false);
                crewMembers.set(i, "");
            }
        }

        production.setMembers(crewMembers);

        // Save the updated production and candidate
        this.productionRepository.save(production);
        this.candidateRepository.save(candidate);

        return ResponseEntity.status(HttpStatus.OK).body("The member was removed.");

    }

    /**
     * Deletes a production according to a specified ID. Throws a bad request exception if there is no matching production.
     * Accepts HTTP DELETE requests at the "./delete/{id}" API endpoint.
     * @param id - An integer identifying the production to be deleted provided as a path variable.
     * Returns a response code of OK with a message stating that the production was deleted if the deletion is successful.
     */
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

    /**
     * Deletes all the productions in the repository. A successful deletion returns a response code of OK and a message
     * indicating that all productions have been deleted.
     * Accepts HTTP DELETE requests at the "./deleteAll" API endpoint.
     */
    @DeleteMapping("/deleteAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All productions have been deleted.")
    public void deleteAllProductions() {
        this.productionRepository.deleteAll();
    }

}
