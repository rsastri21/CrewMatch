package com.lux.crewmatch.repositories;

import com.lux.crewmatch.entities.Production;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductionRepository extends CrudRepository<Production, Integer> {
    Production findByName(String name);
    List<Production> findByProdLeadIsNullAndArchivedFalse();
    List<Production> findByProdLeadIsNotNull();
    List<Production> findByArchived(Boolean archived);

}
