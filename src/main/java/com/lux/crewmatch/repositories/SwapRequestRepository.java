package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.SwapRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SwapRequestRepository extends CrudRepository<SwapRequest, Integer> {
    List<SwapRequest> findByFromLead(String fromLead);
    List<SwapRequest> findByToLead(String toLead);
    List<SwapRequest> findByToLeadAndFromLead(String toLead, String fromLead);
}
