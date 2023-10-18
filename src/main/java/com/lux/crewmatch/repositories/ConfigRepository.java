package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Config;
import org.springframework.data.repository.CrudRepository;

public interface ConfigRepository extends CrudRepository<Config, Integer> {
    Config findByName(String name);
}
