package com.lux.crewmatch.controllers;


import com.lux.crewmatch.entities.Candidate;
import com.lux.crewmatch.entities.Production;
import com.lux.crewmatch.entities.User;
import com.lux.crewmatch.repositories.ProductionRepository;
import com.lux.crewmatch.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final ProductionRepository productionRepository;
    private final BCryptPasswordEncoder encoder;
    private static final String DEFAULT_ROLE = "user";

    // Dependency Injection
    public UserController(UserRepository userRepository, ProductionRepository productionRepository) {
        this.userRepository = userRepository;
        this.productionRepository = productionRepository;
        this.encoder = new BCryptPasswordEncoder(10);
    }

    // Get all registered users
    @GetMapping("/get")
    public Iterable<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    // Register a new user
    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.CREATED, reason = "The user has been registered successfully.")
    public void registerUser(@RequestBody User user) {
        // Check to see if the user is already registered
        Optional<User> userOptional = Optional.ofNullable(this.userRepository.findByUsername(user.getUsername()));
        if (userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That user is already registered, please log in.");
        }

        // Create and store new user
        User userToRegister = new User();

        if (user.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username was not provided.");
        }
        userToRegister.setUsername(user.getUsername());

        if (user.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password was not provided.");
        }
        // Password storage with hashing
        String encodedPassword = encoder.encode(user.getPassword());
        userToRegister.setPassword(encodedPassword);

        // Set the role to user by default
        userToRegister.setRole(DEFAULT_ROLE);

        // Save the user
        this.userRepository.save(userToRegister);

    }

    // Log a user in
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        // Find the user
        Optional<User> userOptional = Optional.ofNullable(this.userRepository.findByUsername(user.getUsername()));
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That user has not been registered.");
        }
        User userToLogin = userOptional.get();

        // Validate the password
        boolean validPassword = encoder.matches(user.getPassword(), userToLogin.getPassword());

        if (!validPassword) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username and password do not match.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userToLogin.getRole());

    }

    // Update a user - Should only be used to update the role of a user
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestParam(name = "username") String username,
                                             @RequestParam(name = "role") String role) {
        Optional<User> userOptional = Optional.ofNullable(this.userRepository.findByUsername(username));
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "That user does not exist.");
        }
        User userToUpdate = userOptional.get();

        // Update the role
        userToUpdate.setRole(role);

        // Save
        this.userRepository.save(userToUpdate);

        return ResponseEntity.status(HttpStatus.OK).body("The user's permissions have been updated.");

    }

    // Assign a production to a production head, i.e. the production head now leads that production.
    @PutMapping("/assign")
    public ResponseEntity<String> assignProdHead(@RequestParam(name = "username") String username,
                                                 @RequestParam(name = "production") String production) {
        Optional<User> userOptional = Optional.ofNullable(this.userRepository.findByUsername(username));
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "That user does not exist.");
        }
        User userToAssign = userOptional.get();

        // If the user that is becoming a production lead is not already a production head, update the role.
        if (userToAssign.getRole().equals("user")) {
            userToAssign.setRole("production head");
        }

        // If the user is already a production lead, remove the association.
        String currentProd = userToAssign.getLeads();
        Optional<Production> currentOptional = Optional.ofNullable(this.productionRepository.findByName(currentProd));
        if (currentOptional.isPresent()) {
            Production current = currentOptional.get();
            current.setProdLead(null);
            // Save
            this.productionRepository.save(current);
        }

        // Validate that the intended production exists.
        Optional<Production> productionOptional = Optional.ofNullable(this.productionRepository.findByName(production));
        if (productionOptional.isEmpty()) {
            // If production does not exist, set leads property to empty.
            userToAssign.setLeads("");
            this.userRepository.save(userToAssign);
            return ResponseEntity.status(HttpStatus.OK).body(userToAssign.getUsername() + " is no longer the lead of any production.");
        }
        Production productionToUpdate = productionOptional.get();

        // Set the user as the lead of the production.
        userToAssign.setLeads(production);
        productionToUpdate.setProdLead(userToAssign.getUsername());

        // Save updates.
        this.userRepository.save(userToAssign);
        this.productionRepository.save(productionToUpdate);

        return ResponseEntity.status(HttpStatus.OK).body(userToAssign.getUsername() + " is now the production lead of " +
                productionToUpdate.getName());
    }

    // Delete a user
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The candidate has been deleted.")
    public void deleteCandidate(@PathVariable("id") Integer id) {
        Optional<User> userToDeleteOptional = this.userRepository.findById(id);

        if (userToDeleteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no user with that ID.");
        }

        User userToDelete = userToDeleteOptional.get();

        // If user leads a production, remove the association.
        if (userToDelete.getLeads() != null) {
            Optional<Production> productionOptional = Optional.ofNullable(this.productionRepository.findByName(userToDelete.getLeads()));
            if (productionOptional.isPresent()) {
                Production productionToUpdate = productionOptional.get();
                productionToUpdate.setProdLead("");
                this.productionRepository.save(productionToUpdate);
            }
        }

        this.userRepository.delete(userToDelete);
    }

    // Delete all users
    @DeleteMapping("/deleteAll")
    @ResponseStatus(code = HttpStatus.OK, reason = "All users have been deleted.")
    public void deleteAll() {
        this.userRepository.deleteAll();
    }

}
