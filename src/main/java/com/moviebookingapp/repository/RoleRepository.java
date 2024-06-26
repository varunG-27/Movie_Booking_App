package com.moviebookingapp.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.moviebookingapp.entity.Roles;
import com.moviebookingapp.entity.Role;

public interface RoleRepository extends MongoRepository<Role, String>{
	Optional<Role> findByName(Roles name);
}
