package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Candidate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Defines the CandidateRepository in which candidate entities are stored. Queries are structured according to JPA
 * guidelines and find stored entities according to the parameters specified in the query title.
 */
public interface CandidateRepository extends CrudRepository<Candidate, Integer> {
    List<Candidate> findByAssignedFalse();
    List<Candidate> findByAssignedTrue();
    List<Candidate> findByActingInterestTrue();
    List<Candidate> findByAssignedFalseAndActingInterestFalse();
    List<Candidate> findByAssignedTrueAndActingInterestFalse();
    List<Candidate> findByAssignedFalseAndActingInterestTrue();
    List<Candidate> findByAssignedTrueAndActingInterestTrue();
    List<Candidate> findByAssignedFalseAndProductionsContaining(String production);
    List<Candidate> findByAssignedFalseAndActingInterestTrueAndProductionsLike(String production);
    List<Candidate> findByAssignedFalseAndActingInterestFalseAndProductionsContaining(String production);
    Candidate findByName(String name);
}
