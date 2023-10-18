package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Configs;
import org.springframework.data.repository.CrudRepository;

public interface ConfigRepository extends CrudRepository<Configs, Integer> {
    Configs findByName(String name);
}
