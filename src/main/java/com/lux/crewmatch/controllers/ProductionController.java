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

@CrossOrigin
@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionRepository productionRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    MatchService matchService;

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
     * Gets the number of productions currently stored in the repository at the "./getCount" API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns a ResponseEntity with an OK status code. The body of the response
     * contains the integer count of productions.
     */
    @GetMapping("/getCount")
    public ResponseEntity<Integer> getNumberOfCandidates() {
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
            productionToUpdate.setMembers(p.getMembers());
        }

        return this.productionRepository.save(productionToUpdate);
    }

    /**
     * Swaps members between two productions. Throws bad request exceptions if any of the productions or members
     * specified are not present.
     * Accepts HTTP PUT requests at the "./swap/..." API endpoint.
     * @param production1 - A string identifying the first production that a member is to be swapped from.
     *                    Provided as a path variable.
     * @param member1 - A string identifying the first member of the swap. Provided as a path variable.
     * @param production2 - A string identifying the second production. Provided as path variable.
     * @param member2 - A string identifying the second member in th swap. Provided as a path variable.
     * @return - Returns a ResponseEntity indicating whether the swap was successful.
     */
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


    /**
     * A helper method that checks if the member specified is present in the given production.
     * @param production - A production instance from which the member will be checked.
     * @param memberToCheck - A string identifying the member's name.
     * @return - A boolean that is true when the member is present and false when not.
     */
    private boolean checkMemberPresent(Production production, String memberToCheck) {
        for (String member : production.getMembers()) {
            if (member.equals(memberToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A helper method that peforms a single swap on a production and saves the updated production.
     * @param production - A production instance in which members will be swapped.
     * @param member1 - A string which represents the name of the first member that is to be found in the production.
     * @param member2 - A string which represents the name of the second member that is to be swapped into the production.
     */
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
