package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.services.CSVHelper;
import com.lux.crewmatch.message.ResponseMessage;
import com.lux.crewmatch.repositories.CandidateRepository;
import com.lux.crewmatch.services.CSVService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateRepository candidateRepository;

    @Autowired
    CSVService fileService;

    /**
     * Creates an instance of the Candidate Controller to handle requests handling candidates.
     * The purpose of this constructor is to configure the proper dependency injection for Spring Boot.
     * @param candidateRepository - The CandidateRepository injected into the controller instance for repository functions.
     */
    public CandidateController(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    /**
     * Gets all candidates currently stored in the repository at the "./get" API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns an iterable containing all the candidates.
     */
    @GetMapping("/get")
    public Iterable<Candidate> getAllCandidates() {
        return this.candidateRepository.findAll();
    }

    /**
     * Gets the number of candidates currently stored in the repository at the "./getCount" API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns a ResponseEntity with an OK status code. The body of the response
     * contains the integer count of candidates.
     */
    @GetMapping("/getCount")
    public ResponseEntity<Integer> getNumberOfCandidates() {
        return ResponseEntity.status(HttpStatus.OK).body((int) this.candidateRepository.count());
    }

    /**
     * Gets the candidate corresponding to a particular id from the repository at the "./get/{id}" API endpoint.
     * Accepts HTTP GET requests.
     * @param id - Accepts an integer as a path variable to specify the candidate to be returned.
     * @return - Returns the appropriate candidate entity.
     */
    @GetMapping("/get/{id}")
    public Candidate getCandidateById(@PathVariable("id") Integer id) {
        Optional<Candidate> candidate = this.candidateRepository.findById(id);

        if (candidate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate matching that ID.");
        }

        return candidate.get();
    }

    /**
     * Gets statistics on the percentage of candidates that are assigned to a production at the "./get/percentAssigned"
     * API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns a ResponseEntity with an OK status code with the body containing the percentage. The percentage
     * is formatted as "XX.xx" between 0 and 100 and not expressed as a decimal.
     */
    @GetMapping("/get/percentAssigned")
    public ResponseEntity<Double> getPercentAssigned() {
        List<Candidate> assignedCandidates = this.candidateRepository.findByAssignedTrue();
        int numAssigned = assignedCandidates.size();

        int numCandidates = (int) this.candidateRepository.count();

        double percent = (double) Math.round((double) 100 * numAssigned / numCandidates * 100) / 100;
        return ResponseEntity.status(HttpStatus.OK).body(percent);
    }

    /**
     * Gets statistics on the percentage of candidates interested in acting at the "./get/percentActing" API endpoint.
     * Accepts HTTP GET requests.
     * @return - Returns a ResponseEntity with an OK status code with the body containing the percentage. The percentage
     * is formatted as "XX.xx" between 0 and 100 and not expressed as a decimal.
     */
    @GetMapping("/get/percentActing")
    public ResponseEntity<Double> getPercentActing() {
        List<Candidate> actingCandidates = this.candidateRepository.findByActingInterestTrue();
        int numActing = actingCandidates.size();

        int numCandidates = (int) this.candidateRepository.count();

        double percent = (double) Math.round((double) 100 * numActing / numCandidates * 100) / 100;
        return ResponseEntity.status(HttpStatus.OK).body(percent);
    }

    /**
     * Gets candidates by specified search parameters. The parameters are included in the HTTP request as query parameters.
     * Accepts HTTP GET requests at the "./search" API endpoint.
     * @param assigned - A boolean specifying whether a candidate is assigned to a production.
     * @param actingInterest - A boolean specifying whether a candidate is interested in acting.
     * @param production - A string identifying a production that should be contained in a candidate's preferences.
     * @return - Returns a list of candidates matching the search criteria.
     */
    @GetMapping("/search")
    public List<Candidate> searchCandidates(
            @RequestParam(name = "assigned", required = false) Boolean assigned,
            @RequestParam(name = "actingInterest", required = false) Boolean actingInterest,
            @RequestParam(name = "production", required = false) String production) {
        if (production != null && assigned != null && actingInterest != null) {
            if (actingInterest && !assigned) {
                return this.candidateRepository.findByAssignedFalseAndActingInterestTrueAndProductionsLike("%" + production + "%");
            }
            if (!actingInterest && !assigned) {
                return this.candidateRepository.findByAssignedFalseAndActingInterestFalseAndProductionsContaining(production);
            }
        }
        if (assigned != null && actingInterest != null) {
            if (!assigned && !actingInterest) {
                return this.candidateRepository.findByAssignedFalseAndActingInterestFalse();
            }
            if (assigned && !actingInterest) {
                return this.candidateRepository.findByAssignedTrueAndActingInterestFalse();
            }
            if (!assigned) {
                return this.candidateRepository.findByAssignedFalseAndActingInterestTrue();
            }
            return this.candidateRepository.findByAssignedTrueAndActingInterestTrue();
        }
        if (assigned != null) {
            return assigned ? this.candidateRepository.findByAssignedTrue() : this.candidateRepository.findByAssignedFalse();
        }
        if (actingInterest != null && actingInterest) {
            return this.candidateRepository.findByActingInterestTrue();
        }
        return new ArrayList<>();
    }

    /**
     * Gets a candidate by name.
     * Accepts HTTP GET requests at the "./getByName" API endpoint.
     * @param name - A string name identifying the candidate provided as a parameter in the request.
     * @return - Returns the candidate entity if it exists, throws an exception if not.
     */
    @GetMapping("/getByName")
    public Candidate getCandidateByName(@RequestParam(name = "name") String name) {
        Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(name));
        if (candidateOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate with that name");
        }
        return candidateOptional.get();
    }

    /**
     * Creates a new candidate with the specified characteristics included in the request body.
     * Checks to see if the candidate has already been created. If so, the candidate is updated with the provided parameters
     * instead of creating a new instance.
     * Accepts HTTP POST requests at the "./add" API endpoint.
     * @param candidate - A serialized instance of the candidate entity that is to be added to the repository.
     * @return - Returns the instance of the candidate that is saved in the repository.
     */
    @PostMapping("/add")
    public Candidate createNewCandidate(@RequestBody Candidate candidate) {
        // First see if candidate exists already
        Optional<Candidate> candidateOptional = Optional.ofNullable(this.candidateRepository.findByName(candidate.getName()));

        if (candidateOptional.isPresent()) {
            Candidate candidateToUpdate = candidateOptional.get();
            CSVService.updateCandidate(candidate, candidateToUpdate);
            return this.candidateRepository.save(candidateToUpdate);
        } else {
            return this.candidateRepository.save(candidate);
        }
    }

    /**
     * Creates candidates in bulk as read off from a CSV file.
     * Accepts HTTP POST requests at the "./upload" API endpoint.
     * @param file - A CSV file that is intended to be the results of a LUX Role Interest Form.
     * @return - Returns a ResponseEntity with a message outlining the success of the file upload.
     * If the file uploaded is not a CSV file, a BAD_REQUEST is returned. Input processing is carried out to avoid
     * accidental or incorrect uploads possibly corrupting other data.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.isValidFile(file)) {
            try {
                fileService.save(file);

                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(message);
            } catch (Exception e) {
                message = e.getMessage();
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
            }
        }

        message = "Please upload a CSV file.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);

    }


    /**
     * Updates a candidate according to parameters specified in the request body. A bad request exception is thrown if
     * there is no candidate matching the inputted ID.
     * Accepts HTTP PUT requests at the "./update/{id}" API endpoint.
     * @param id - An integer identifying a candidate to update that is provided as a path variable.
     * @param c - The candidate body with parameters that are to be updated in the existing instance.
     * @return - Returns the updated instance that is now saved in the repository.
     */
    @PutMapping("/update/{id}")
    public Candidate updateCandidateById(@PathVariable("id") Integer id, @RequestBody Candidate c) {
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
        CSVService.updateCandidate(c, candidateToUpdate);

        return this.candidateRepository.save(candidateToUpdate);

    }

    /**
     * Deletes a candidate according to a specified ID. Throws a bad request exception if there is no candidate matching
     * the ID provided.
     * Accepts HTTP DELETE requests at the "./delete/{id}" API endpoint.
     * @param id - An integer identifying a candidate that is to be deleted.
     * If the deletion is successful, an OK response is returned with a message stating the candidate was deleted.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The candidate has been deleted.")
    public void deleteCandidate(@PathVariable("id") Integer id) {
        Optional<Candidate> candidateToDeleteOptional = this.candidateRepository.findById(id);

        if (candidateToDeleteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no candidate with that ID.");
        }

        Candidate candidateToDelete = candidateToDeleteOptional.get();
        this.candidateRepository.delete(candidateToDelete);
    }

    /**
     * Deletes all the candidates in the repository. A successful deletion returns a response code of OK and a message
     * indicating that all candidates have been deleted.
     * Accepts HTTP DELETE requests at the "./deleteAll" API endpoint.
     */
    @DeleteMapping("/deleteAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All candidates have been deleted.")
    public void deleteAll() {
        this.candidateRepository.deleteAll();
    }

}
