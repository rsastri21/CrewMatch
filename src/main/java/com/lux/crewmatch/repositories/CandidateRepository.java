package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Candidate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CandidateRepository extends CrudRepository<Candidate, Integer> {
    List<Candidate> findByAssignedFalse();
}
