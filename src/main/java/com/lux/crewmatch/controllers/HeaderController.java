package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Header;
import com.lux.crewmatch.repositories.HeaderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/headers")
public class HeaderController {

    private final HeaderRepository headerRepository;

    /**
     * Creates an instance of the HeaderController to handle requests related to changing the CSV headers.
     * The purpose of this constructor is to properly configure the dependency injection of the repository
     * so that it is available for each of the requests.
     * This controller follows the Singleton design pattern, where only one header object is stored to maintain the
     * active configuration.
     * @param headerRepository - The repository where the headers are stored.
     */
    public HeaderController(HeaderRepository headerRepository) {
        this.headerRepository = headerRepository;
    }

    /**
     * Gets the current header configuration from the repository.
     * Accepts HTTP GET requests at the "./get" API endpoint.
     * Throws an exception if there are no headers created.
     * @return - Returns the Singleton header object which contains the current CSV headers for the active
     * configuration.
     */
    @GetMapping("/get")
    public ResponseEntity<Header> getHeaders() {
        Optional<Header> headerOptional = Optional.ofNullable(this.headerRepository.findByName("header"));
        if (headerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No headers have been initialized.");
        }
        Header header = headerOptional.get();

        return ResponseEntity.status(HttpStatus.OK).body(header);
    }

    /**
     * Creates a new header to store a new active configuration. If a configuration has already been created, it
     * overwrites the existing one to maintain the persistence of a single header object.
     * Accepts HTTP POST requests at the "./update" API endpoint with a serialized header object in the request body.
     * Throws an exception if the title of the serialized object is not "header", or if the appropriate number of headers
     * are not provided.
     * @param header - A header object with the new configuration to be stored.
     * @return - Returns a ResponseEntity indicating the status of the creation.
     */
    @PostMapping("/update")
    public ResponseEntity<Header> updateHeaders(@RequestBody Header header) {
        if (!header.getName().equals("header")) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Please title the headers \"header\" to ensure" +
                    "data is stored efficiently.");
        }
        if (header.getCsvHeaders().size() != 15) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Please provide the appropriate number of " +
                    "headers.");
        }

        // Update header if it already exists
        Optional<Header> headerOptional = Optional.ofNullable(this.headerRepository.findByName("header"));
        if (headerOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(this.headerRepository.save(header));
        }

        Header headerToUpdate = headerOptional.get();
        headerToUpdate.setCsvHeaders(header.getCsvHeaders());
        return ResponseEntity.status(HttpStatus.OK).body(this.headerRepository.save(headerToUpdate));

    }


}
