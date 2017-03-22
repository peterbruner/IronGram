package com.theironyard.novauc.services;

import com.theironyard.novauc.entities.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Integer> {
    User findFirstByName(String name);
}
