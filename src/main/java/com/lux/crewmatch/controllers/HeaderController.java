package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Header;
import com.lux.crewmatch.repositories.HeaderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/headers")
public class HeaderController {

    private final HeaderRepository headerRepository;

    // Dependency Injection
    public HeaderController(HeaderRepository headerRepository) {
        this.headerRepository = headerRepository;
    }

    // Get the current headers.
    @GetMapping("/get")
    public ResponseEntity<Header> getHeaders() {
        Optional<Header> headerOptional = Optional.ofNullable(this.headerRepository.findByName("header"));
        if (headerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No headers have been initialized.");
        }
        Header header = headerOptional.get();

        return ResponseEntity.status(HttpStatus.OK).body(header);
    }

    // Create a new set of headers
    // Should update the existing set if there is an entry stored
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
