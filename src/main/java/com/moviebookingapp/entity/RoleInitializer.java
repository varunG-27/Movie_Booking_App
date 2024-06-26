package com.moviebookingapp.entity;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.moviebookingapp.repository.RoleRepository;

@Component
public class RoleInitializer {

	private final RoleRepository roleRepository;

	@Autowired
	public RoleInitializer(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@PostConstruct
	public void initializeRoles() {
		// Check if the "ROLE_USER" role exists
		if (!roleRepository.findByName(Roles.ROLE_USER).isPresent()) {
			// If not, insert it into the database
			Role userRole = new Role(Roles.ROLE_USER);
			roleRepository.save(userRole);
		}
		// Similarly, you can initialize other roles here if needed
		if (!roleRepository.findByName(Roles.ROLE_ADMIN).isPresent()) {
			// If not, insert it into the database
			Role userRole = new Role(Roles.ROLE_ADMIN);
			roleRepository.save(userRole);
		}

	}
}
