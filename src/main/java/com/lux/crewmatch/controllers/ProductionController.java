package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.entities.SwapRequest;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.repositories.ProductionRepository;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.repositories.SwapRequestRepository;
import com.lux.crewmatch.services.CSVService;
import com.lux.crewmatch.services.MatchService;
import com.lux.crewmatch.services.WeightedMatchService;
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
    WeightedMatchService weightedMatchService;

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
     * Gets all active productions stored in the production repository.
     * Accepts HTTP GET requests at the "./get" API endpoint.
     * @return - Returns a list containing all the productions currently stored in the production repository.
     */
    @GetMapping("/get")
    public List<Production> getAllProductions() {
        return this.productionRepository.findByArchived(false);
    }

    /**
     * Gets all archived productions stored in the production repository.
     * Accepts HTTP GET requests at the "./getArchived" API endpoint.
     * @return - Returns a list containing all the archived productions currently stored in the production repository.
     */
    @GetMapping("/getArchived")
    public List<Production> getAllArchivedProductions() { return this.productionRepository.findByArchived(true); }

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
        return ResponseEntity.status(HttpStatus.OK).body(this.productionRepository.findByArchived(false).size());
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
     * Gets all the roles that contained within any active production.
     * Intended for use prior to creating the Role Interest Form, allowing all candidates
     * to select roles that are contained within a production's needs.
     * Accepts HTTP GET requests at the "./get/roles" API endpoint.
     * @return - Returns a list of strings containing all the roles contained on all the productions.
     */
    @GetMapping("/get/roles")
    public List<String> getAllRoles() {
        List<Production> productions = this.productionRepository.findByArchived(false);

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

        InputStreamResource fileInputStream = fileService.dataToCSV(this.productionRepository.findByArchived(false));

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
     * Matches candidates to productions taking the role weightings into consideration.
     * Accepts HTTP GET requests at the "./weightedMatch" API endpoint.
     * @return - Returns a ResponseEntity with a message containing how many candidates were matched and how many
     * remain to be matched. Returns an error message if there are no candidates or no productions to match.
     */
    @GetMapping("/weightedMatch")
    public ResponseEntity<String> weightedMatchCandidatesToProductions() {
        return weightedMatchService.weightedMatch();
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
        // Ensure that the weights list is provided
        if (production.getRoleWeights() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No weights have been provided for the roles list.");
        }
        // Prior to saving, check if members in the production are not created candidates yet
        List<String> crew = production.getMembers();
        validateCandidateSet(crew, production);

        // Capitalize all crew member names
        List<String> crewFormatted = new ArrayList<>(crew);
        for (int i = 0; i < crew.size(); i++) {
            crewFormatted.set(i, CSVHelper.formatName(crew.get(i)));
        }
        production.setMembers(new ArrayList<>(crewFormatted));

        // Normalize inputted weights
        production.normalize();

        // Set archived field to false
        production.setArchived(false);

        return ResponseEntity.status(HttpStatus.OK).body(this.productionRepository.save(production));
    }

    /**
     * A helper method that ensures the current candidate set is up-to-date with new candidates
     * potentially added upon creation of a new production.
     * @param crew - A list specifying the production crew members as strings.
     * @param production - The production from which the crew is to be validated.
     */
    private void validateCandidateSet(List<String> crew, Production production) {
        for (int i = 0; i < crew.size(); i++) {
            String member = crew.get(i);
            // See if candidate exists, create an entry if not
            String memberName = CSVHelper.formatName(member);
            Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(memberName));
            if (candidateOptional.isEmpty()) {
                Candidate candidateToAdd = new Candidate();
                if (memberName.equals("")) {
                    continue;
                }
                candidateToAdd.setName(memberName);
                candidateToAdd.assign(production, production.getRoles().get(i));
                candidateToAdd.setActingInterest(false);

                this.candidateRepository.save(candidateToAdd);
            } else {
                // If the candidate already exists, set assigned property to true
                Candidate candidateToUpdate = candidateOptional.get();
                candidateToUpdate.assign(production, production.getRoles().get(i));
                this.candidateRepository.save(candidateToUpdate);
            }
        }
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
        if (p.getArchived() != null) {
            productionToUpdate.setArchived(p.getArchived());
        }
        if (p.getRoles() != null) {
            productionToUpdate.setRoles(p.getRoles());
        }
        if (p.getRoleWeights() != null) {
            productionToUpdate.setRoleWeights(p.getRoleWeights());
        }
        if (p.getMembers() != null) {

            // Set all members who are currently assigned to unassigned
            // Purpose is to revalidate with the second iteration
            for (int i = 0; i < productionToUpdate.getMembers().size(); i++) {
                String member = productionToUpdate.getMembers().get(i);
                // Pull candidate from repository
                Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(member));
                // Set assigned to false if candidate is present
                if (candidateOptional.isPresent()) {
                    Candidate candidate = candidateOptional.get();
                    candidate.unassign(productionToUpdate, productionToUpdate.getRoles().get(i));
                    this.candidateRepository.save(candidate);
                }
            }

            List<String> members = new ArrayList<>(p.getMembers());

            // Iterate through candidates in updated production to validate with existing store
            for (int i = 0; i < members.size(); i++) {
                String member = members.get(i);
                // If string is empty, skip
                if (member.equals("")) {
                    continue;
                }
                // Pull the candidate from repository
                Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(member));
                if (candidateOptional.isPresent()) {
                    // Set candidate assigned field to true
                    Candidate candidate = candidateOptional.get();
                    candidate.assign(productionToUpdate, p.getRoles().get(i));
                    this.candidateRepository.save(candidate);
                } else {
                    // Candidate not present -> Create a new skeleton candidate
                    Candidate newCandidate = new Candidate();
                    newCandidate.setName(member);
                    newCandidate.assign(productionToUpdate, p.getRoles().get(i));
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

        // Assign the candidate to the requested role if it is empty
        List<String> productionMembers = new ArrayList<>(productionToUpdate.getMembers());
        if (!productionMembers.get(roleIndex).equals("")) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("The desired role is already filled");
        }
        productionMembers.set(roleIndex, candidateToAssign.getName());

        // Update production
        productionToUpdate.setMembers(productionMembers);
        this.productionRepository.save(productionToUpdate);

        // Change candidate status to assigned
        candidateToAssign.assign(productionToUpdate, productionToUpdate.getRoles().get(roleIndex));
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
                candidate.unassign(production, production.getRoles().get(i));
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
     * Archives a production and removes all members allowing them to be assigned to other productions.
     * Accepts HTTP PUT requests at the "./archive/{id}" API endpoint.
     * @param id - An integer identifying the production to be archived.
     * Returns a response code of OK if the archive was successful.
     */
    @PutMapping("/archive/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The production has been archived.")
    public void archiveProduction(@PathVariable("id") Integer id) {
        Optional<Production> productionToArchiveOptional = this.productionRepository.findById(id);

        if (productionToArchiveOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }
        Production productionToArchive = productionToArchiveOptional.get();
        // No need to do anything if production is already archived
        if (productionToArchive.getArchived()) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "The production is already archived.");
        }

        deleteCandidatesFromProduction(productionToArchive);

        productionToArchive.setArchived(true);
        this.productionRepository.save(productionToArchive);
    }

    /**
     * Archives all productions and removes all candidates matched to them.
     * Accepts HTTP PUT requests at the "./archiveALl" API endpoint.
     * Returns a 200 OK response if the request is successful.
     */
    @PutMapping("/archiveAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All productions have been archived.")
    public void archiveAllProductions() {
        // Get all active productions
        List<Production> productionsList = this.productionRepository.findByArchived(false);

        // Remove candidates and set archived flag for each production
        for (Production production : productionsList) {
            deleteCandidatesFromProduction(production);
            production.setArchived(true);
            this.productionRepository.save(production);
        }
    }

    /**
     * Restores a production from the archive and validates that all candidates on that archived production
     * are present. These candidates will be created if they are not present to ensure a valid candidate set.
     * @param id - An integer representing the ID of the production to be restored.
     */
    @PutMapping("/restore/{id}")
    @ResponseStatus(code = HttpStatus.ACCEPTED, reason = "The production has been restored from the archive.")
    public void restoreProduction(@PathVariable("id") Integer id) {
        Optional<Production> productionToRestoreOptional = this.productionRepository.findById(id);

        if (productionToRestoreOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no production matching that id.");
        }
        Production productionToRestore = productionToRestoreOptional.get();
        if (!productionToRestore.getArchived()) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "The production is not archived.");
        }

        // Reinitialize crew members
        validateCandidateSet(productionToRestore.getMembers(), productionToRestore);

        // Update production parameters and save
        productionToRestore.setArchived(false);
        this.productionRepository.save(productionToRestore);
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
        deleteCandidatesFromProduction(productionToDelete);

        this.productionRepository.delete(productionToDelete);
    }

    /**
     * Helper method to unassign all candidates on a production.
     * @param production - The production from which candidates are to be removed.
     */
    private void deleteCandidatesFromProduction(Production production) {
        // Obtain candidates list to be updated
        List<String> crewMembers = production.getMembers();

        for (int i = 0; i < crewMembers.size(); i++) {
            String member = crewMembers.get(i);

            // Get the candidate from the repository
            Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(member));
            if (candidateOptional.isEmpty()) {
                continue;
            }
            Candidate candidate = candidateOptional.get();

            // Unassign the candidate from their role on the production, so they are available for future matches
            candidate.unassign(production, production.getRoles().get(i));
            this.candidateRepository.save(candidate);
        }
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
