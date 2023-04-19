package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.entities.SwapRequest;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.repositories.ProductionRepository;
import com.lux.crewmatch.repositories.SwapRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/swap")
public class SwapRequestController {

    private final SwapRequestRepository swapRequestRepository;
    private final ProductionRepository productionRepository;
    private final CandidateRepository candidateRepository;

    /**
     * Creates an instance of the SwapRequestController for handling swap requests. Configures dependency injection so
     * repositories are accessible to other API endpoints.
     * @param swapRequestRepository - An instance of the repository where swap requests are stored.
     * @param productionRepository - An instance of the repository where productions are stored.
     * @param candidateRepository - An instance of the repository where candidates are stored.
     */
    public SwapRequestController(SwapRequestRepository swapRequestRepository, ProductionRepository productionRepository, CandidateRepository candidateRepository) {
        this.swapRequestRepository = swapRequestRepository;
        this.productionRepository = productionRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Gets all submitted swap requests.
     * Accepts HTTP GET requests at the "./get" API endpoint.
     * @return - Returns an iterable containing all of the current swap requests.
     */
    @GetMapping("/get")
    public Iterable<SwapRequest> getAllSwapRequests() {
        return this.swapRequestRepository.findAll();
    }

    /**
     * Gets a particular swap request by its ID.
     * Accepts HTTP GET requests at the "./get/{id}" API endpoint.
     * Throws an exception if there is no matching swap request to the provided ID.
     * @param id - An integer ID that specifies a particular swap request, provided as a path variable.
     * @return - Returns a swap request matching the ID.
     */
    @GetMapping("/get/{id}")
    public SwapRequest getSwapRequestById(@PathVariable("id") Integer id) {
        Optional<SwapRequest> swapRequest = this.swapRequestRepository.findById(id);

        // Check that request exists
        if (swapRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no swap request matching that ID.");
        }

        return swapRequest.get();
    }

    /**
     * Creates a new swap request.
     * Accepts HTTP POST requests at the "./create" API endpoint.
     * @param request - A serialized SwapRequest entity provided in the request body carrying the details of the
     * request.
     * @return - Returns the created SwapRequest instance.
     */
    @PostMapping("/create")
    public SwapRequest createNewSwapRequest(@RequestBody SwapRequest request) {
        // New request to save
        SwapRequest swapRequest = new SwapRequest();
        // Default to not completed
        swapRequest.setCompleted(false);

        // Retrieve fields from request body and update
        if (request.getFromLead() != null) {
            swapRequest.setFromLead(request.getFromLead());
        }
        if(request.getToLead() != null) {
            swapRequest.setToLead(request.getToLead());
        }

        if (request.getProduction1() != null) {
            swapRequest.setProduction1(request.getProduction1());
        }
        if (request.getMember1() != null) {
            swapRequest.setMember1(request.getMember1());
        }
        if (request.getRole1() != null) {
            swapRequest.setRole1(request.getRole1());
        }

        if (request.getProduction2() != null) {
            swapRequest.setProduction2(request.getProduction2());
        }
        if (request.getMember2() != null) {
            swapRequest.setMember2(request.getMember2());
        }
        if (request.getRole2() != null) {
            swapRequest.setRole2(request.getRole2());
        }

        // Save the created request
        return this.swapRequestRepository.save(swapRequest);

    }

    /**
     * Swaps members between two productions. Throws bad request exceptions if any of the productions or members
     * specified are not present. Occurs after accepting a pending swap request
     * Accepts HTTP PUT requests at the "./accept/{id}" API endpoint.
     * @param id - An id for a swap request with the parameters outlined in the entity model.
     * @return - Returns a ResponseEntity indicating whether the swap was successful.
     */
    @PutMapping("/accept/{id}")
    public ResponseEntity<String> acceptSwapRequest(@PathVariable("id") Integer id) {
        // Get the swap request from the repository
        Optional<SwapRequest> swapRequestOptional = this.swapRequestRepository.findById(id);

        if (swapRequestOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no request matching that id.");
        }
        SwapRequest swapRequest = swapRequestOptional.get();

        // Find the first production
        String production1 = swapRequest.getProduction1();
        Optional<Production> productionOptionalOne = Optional.ofNullable(this.productionRepository.findByName(production1));
        if (productionOptionalOne.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no production matching the first name.");
        }
        Production productionOne = productionOptionalOne.get();

        // Find the second production
        String production2 = swapRequest.getProduction2();
        Optional<Production> productionOptionalTwo = Optional.ofNullable(this.productionRepository.findByName(production2));
        if (productionOptionalTwo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no production matching the second name.");
        }
        Production productionTwo = productionOptionalTwo.get();

        // Check members are not missing
        String member1 = swapRequest.getMember1();
        String member2 = swapRequest.getMember2();
        if (!checkMemberPresent(productionOne, member1) || !checkMemberPresent(productionTwo, member2)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One of the members is missing from its production.");
        }

        // Swap members
        swapMembers(productionOne, member1, swapRequest.getRole1(), member2);
        swapMembers(productionTwo, member2, swapRequest.getRole2(), member1);

        // Update the roles that the candidates are assigned to
        Optional<Candidate> candidate1Optional = Optional.ofNullable(this.candidateRepository.findByName(member1));
        if (candidate1Optional.isPresent()) {
            Candidate candidate1 = candidate1Optional.get();
            candidate1.unassign(productionOne, swapRequest.getRole1());
            candidate1.assign(productionTwo, swapRequest.getRole2());
            this.candidateRepository.save(candidate1);
        }

        Optional<Candidate> candidate2Optional = Optional.ofNullable(this.candidateRepository.findByName(member2));
        if (candidate2Optional.isPresent()) {
            Candidate candidate2 = candidate2Optional.get();
            candidate2.unassign(productionTwo, swapRequest.getRole2());
            candidate2.assign(productionOne, swapRequest.getRole1());
            this.candidateRepository.save(candidate2);
        }

        // Save the swap request as completed
        swapRequest.setCompleted(true);
        this.swapRequestRepository.save(swapRequest);

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
            if (member.startsWith(memberToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A helper method that peforms a single swap on a production and saves the updated production.
     * @param production - A production instance in which members will be swapped.
     * @param member1 - A string which represents the name of the first member that is to be found in the production.
     * @param role - A string identifying the role of member1 that member2 is to be swapped into.
     * @param member2 - A string which represents the name of the second member that is to be swapped into the production.
     */
    private void swapMembers(Production production, String member1, String role, String member2) {
        List<String> prodMembers = new ArrayList<>(production.getMembers());
        List<String> prodRoles = production.getRoles();
        for (int i = 0; i < prodMembers.size(); i++) {
            if (prodMembers.get(i).startsWith(member1) && prodRoles.get(i).equals(role)) {
                prodMembers.set(i, member2);
                production.setMembers(prodMembers);
                this.productionRepository.save(production);
                break;
            }
        }
    }

    // Reject a swap request
    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rejectSwapRequest(@PathVariable("id") Integer id) {
        // Retrieve request
        Optional<SwapRequest> swapRequestOptional = this.swapRequestRepository.findById(id);

        if (swapRequestOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no request matching that ID.");
        }
        SwapRequest swapRequest = swapRequestOptional.get();

        // Update the completed field to null to indicate rejection
        swapRequest.setCompleted(null);

        // Save result
        this.swapRequestRepository.save(swapRequest);

        return ResponseEntity.status(HttpStatus.OK).body("The swap request was rejected.");
    }

    // Search for a request
    @GetMapping("/search")
    public List<SwapRequest> searchRequests(@RequestParam(name = "from", required = false) String from,
                                            @RequestParam(name = "to", required = false) String to) {
        if (from != null && to != null) {
            return this.swapRequestRepository.findByToLeadAndFromLead(to, from);
        }
        if (from != null) {
            return this.swapRequestRepository.findByFromLead(from);
        }
        if (to != null) {
            return this.swapRequestRepository.findByToLead(to);
        }
        return new ArrayList<>();
    }

    // Delete by ID
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The swap request has been deleted.")
    public void deleteRequest(@PathVariable("id") Integer id) {
        Optional<SwapRequest> requestToDeleteOptional = this.swapRequestRepository.findById(id);

        if (requestToDeleteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no request with that ID.");
        }

        SwapRequest requestToDelete = requestToDeleteOptional.get();
        this.swapRequestRepository.delete(requestToDelete);
    }

    // Delete all requests
    @DeleteMapping("/deleteAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All requests have been deleted.")
    public void deleteAll() {
        this.swapRequestRepository.deleteAll();
    }

}
