package com.lux.crewmatch.repositories;


import com.lux.crewmatch.entities.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Defines the User repository that is used to store users of the application. Query methods are defined according to
 * JPA guidelines.
 */
public interface UserRepository extends CrudRepository<User, Integer> {

}
