package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ulake.api.models.User;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.RoleRepository;
import com.ulake.api.repository.UserRepository;
import java.util.Optional;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@GetMapping("/check_username")
	ResponseEntity<?> username(
	  @RequestParam("username") String username) {
		if (userRepository.existsByUsername(username)) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}
		
		return ResponseEntity.ok(new MessageResponse("Username is available!"));
	}
	
	@GetMapping("/check_email")
	ResponseEntity<?> email(
	  @RequestParam("email") String email) {
		if (userRepository.existsByEmail(email)) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already taken!"));
		}
		
		return ResponseEntity.ok(new MessageResponse("Email is available!"));
	}
	
	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getAllUsers(){
		try {
			List<User> users = new ArrayList<User>();
			userRepository.findAll().forEach(users::add);
			if (users.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(users, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
			
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUserById(@PathVariable("id") long id){
		Optional<User> userId = userRepository.findById(id);
		if (userId.isPresent()) {
			return new ResponseEntity<>(userId.get(),HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
//	@PutMapping("/{id}")
//	@PreAuthorize("hasRole('ADMIN')")
//	public ResponseEntity<User> updateUserById(@PathVariable("id") long id, @RequestBody User user){
//		Optional<User> userId = userRepository.findById(id);
//		if (userId.isPresent()) {
//			User userInfo = userId.get();
////			userInfo.setFirstname(user.getFirstname());
//			return new ResponseEntity<>(userRepository.save(userInfo),HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//	}
	
//	@DeleteMapping("/{id}")
//	@PreAuthorize("hasRole('ADMIN')")
//	public ResponseEntity<User> deleteUserById(@PathVariable("id") long id){
//		try {
//			userRepository.deleteById(id);
//			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//		} 
//		catch (Exception e) {
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
}
