package com.moviebookingapp.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviebookingapp.entity.JwtResponse;
import com.moviebookingapp.entity.LoginRequest;
import com.moviebookingapp.entity.MessageResponse;
import com.moviebookingapp.entity.Role;
import com.moviebookingapp.entity.Roles;
import com.moviebookingapp.entity.SignUpRequest;
import com.moviebookingapp.entity.User;
import com.moviebookingapp.repository.RoleRepository;
import com.moviebookingapp.repository.UserRepository;
import com.moviebookingapp.security.jwt.JwtUtils;
import com.moviebookingapp.services.UserDetailsImpl;


import io.swagger.v3.oas.annotations.Operation;

@RestController
@CrossOrigin(origins = "http:localhost:4200")
@RequestMapping("/api/v1.0/moviebooking")
public class AuthController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	JwtUtils jwtUtils;
	
	
	
	@PostMapping("/login")
	@Operation(summary = "login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());
		
		return ResponseEntity.ok(new JwtResponse( jwt , 
				userDetails.get_id(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles
				));
		}
	
	@PostMapping("/register")
	@Operation(summary = "New Registration")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
		//Check if username already exist or not
		if(userRepository.existsByLoginId(signUpRequest.getLoginId())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: LoginId is already taken"));
		}
		
		//Check if email exist or not
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}
		
		User user = new User(signUpRequest.getLoginId(),
				signUpRequest.getFirstName(),
				signUpRequest.getLastName(),
				signUpRequest.getEmail(),
				signUpRequest.getContactNumber(),
				encoder.encode(signUpRequest.getPassword()));
		
		Set<String> strRoles = signUpRequest.getRoles();
		Set<Role> roles = new HashSet<>();
		
		String errorMessage = "Error: Roles not found.";
		
		if(strRoles == null) {
			Role userRole = roleRepository.findByName(Roles.ROLE_USER)
					.orElseThrow(() -> new RuntimeException(errorMessage));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch(role) {
				case "admin" : 
					Role adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException(errorMessage));
					roles.add(adminRole);
					break;
					
				case "guest":
					Role modRole = roleRepository.findByName(Roles.ROLE_USER)
						.orElseThrow(() -> new RuntimeException(errorMessage));
					roles.add(modRole);
					break;
					
				default:
						Role userRole = roleRepository.findByName(Roles.ROLE_USER)
							.orElseThrow(() -> new RuntimeException(errorMessage));
						roles.add(userRole);
				}
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		return ResponseEntity.ok(new MessageResponse("User Registered Successfully!"));
	}
	
	@GetMapping("/users")
	public ResponseEntity<?> getDetails(){
		return ResponseEntity.ok(this.userRepository.findAll());
	}
}
