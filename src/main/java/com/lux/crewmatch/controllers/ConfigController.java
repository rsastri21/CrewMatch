package com.lux.crewmatch.controllers;

import com.lux.crewmatch.entities.Configs;
import com.lux.crewmatch.repositories.ConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@CrossOrigin
@RestController()
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigRepository configRepository;

    public ConfigController(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping("/getByName")
    public ResponseEntity<Configs> getConfigByName(@RequestParam(name = "name") String name) {
        Optional<Configs> configOptional = Optional.ofNullable(this.configRepository.findByName(name));
        if (configOptional.isEmpty() && name.equals("maxCrewSize")) {
            Configs config = new Configs();
            config.setName(name);
            config.setValue(24);
            return ResponseEntity.status(HttpStatus.OK).body(config);
        }
        if (configOptional.isEmpty() && name.equals("isRegistrationOpen")) {
            Configs config = new Configs();
            config.setName(name);
            config.setValue(0);
            return ResponseEntity.status(HttpStatus.OK).body(config);
        }
        if (configOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no config variable with that name.");
        }
        Configs config = configOptional.get();

        return ResponseEntity.status(HttpStatus.OK).body(config);
    }

    @PostMapping("/update")
    public ResponseEntity<Configs> updateConfig(@RequestBody Configs config) {
        // Update config variable if it already exists
        Optional<Configs> configOptional = Optional.ofNullable(this.configRepository.findByName(config.getName()));
        if (configOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(this.configRepository.save(config));
        }

        Configs configToUpdate = configOptional.get();
        if (config.getValue() != null) {
            configToUpdate.setValue(config.getValue());
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.configRepository.save(configToUpdate));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(code = HttpStatus.OK, reason = "The config variable has been deleted.")
    public void deleteConfig(@PathVariable("id") Integer id) {
        Optional<Configs> configOptional = this.configRepository.findById(id);
        if (configOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no config variable with that ID.");
        }
        Configs configToDelete = configOptional.get();

        this.configRepository.delete(configToDelete);
    }


}
