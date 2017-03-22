package com.theironyard.novauc.services;


import com.theironyard.novauc.entities.Photo;
import com.theironyard.novauc.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhotoRepository extends CrudRepository<Photo, Integer>{
    List<Photo> findByRecipient(User user);
}
