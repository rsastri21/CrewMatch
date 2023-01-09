package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Header;
import org.springframework.data.repository.CrudRepository;

public interface HeaderRepository extends CrudRepository<Header, Integer> {
    Header findByName(String name);
}
