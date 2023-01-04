package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Candidate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CandidateRepository extends CrudRepository<Candidate, Integer> {
    List<Candidate> findByAssignedFalse();
    List<Candidate> findByAssignedTrue();
    List<Candidate> findByActingInterestTrue();
    List<Candidate> findByAssignedFalseAndActingInterestFalse();
    List<Candidate> findByAssignedTrueAndActingInterestFalse();
    List<Candidate> findByAssignedFalseAndActingInterestTrue();
    List<Candidate> findByAssignedTrueAndActingInterestTrue();
    Candidate findByName(String name);
}
