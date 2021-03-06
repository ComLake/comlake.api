package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ulake.api.models.User;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.RoleRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.impl.UserDetailsImpl;

import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class UserController {
	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	private Sort.Direction getSortDirection(String direction) {
		if (direction.equals("ASC")) {
			return Sort.Direction.ASC;
		} else if (direction.equals("DESC")) {
			return Sort.Direction.DESC;
		}

		return Sort.Direction.ASC;
	}

	@Operation(summary = "Check if Username is available to use", description = "Check if Username is available to use", tags = {
			"Users" })
	@GetMapping("/users/check_username")
	ResponseEntity<?> username(@RequestParam("username") String username) {
		if (userRepository.existsByUsername(username)) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		return ResponseEntity.ok(new MessageResponse("Username is available!"));
	}

	@Operation(summary = "Check if Email is available to use", description = "Check if Email is available to use", tags = {
			"Users" })
	@GetMapping("/users/check_email")
	ResponseEntity<?> email(@RequestParam("email") String email) {
		if (userRepository.existsByEmail(email)) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken!"));
		}

		return ResponseEntity.ok(new MessageResponse("Email is available!"));
	}

	@Operation(summary = "Get a list of all users in the system", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String email,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int perPage,
			@RequestParam(defaultValue = "id,asc") String[] sort) {
		try {
			List<Order> orders = new ArrayList<Order>();

			if (sort[0].contains(",")) {
				// will sort more than 2 fields
				// sortOrder="field, direction"
				for (String sortOrder : sort) {
					String[] _sort = sortOrder.split(",");
					orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
				}
			} else {
				// sort=[field, direction]
				orders.add(new Order(getSortDirection(sort[1]), sort[0]));
			}

			List<User> users = new ArrayList<User>();
			Pageable pagingSort = PageRequest.of(page, perPage, Sort.by(orders));

			Page<User> pageTuts;
			if (email == null)
				pageTuts = userRepository.findAll(pagingSort);
			else
				pageTuts = userRepository.findByEmailContaining(email, pagingSort);

			users = pageTuts.getContent();

			if (users.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

//	        Map<String, Object> response = new HashMap<>();
//	        response.put("users", users);
//	        response.put("currentPage", pageTuts.getNumber());
//	        response.put("total", pageTuts.getTotalElements());
//	        response.put("totalPages", pageTuts.getTotalPages());

			HttpHeaders responseHeaders = new HttpHeaders();
			long l = pageTuts.getTotalElements();
			String total = String.valueOf(l);
			responseHeaders.set("x-total-count", total);

			return new ResponseEntity<>(users, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get user by ID", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@GetMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
		Optional<User> userData = userRepository.findById(id);
		if (userData.isPresent()) {
			return new ResponseEntity<>(userData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get user by user name", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "400", description = "Invalid username supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@GetMapping("/users/find/{username}")
	@PreAuthorize("hasRole('ADMIN')")
	public List<User> getUserByUsername(@PathVariable("username") String username) {
		List<User> user = userRepository.findByUsernameContaining(username);
		return user;
	}

	@Operation(summary = "Get the logged in's user profile", description = "This can only be done by the logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@GetMapping("/users/my-profile")
	public ResponseEntity<User> getCurrentProfile() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		long userId = userDetails.getId();
		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent()) {
			return new ResponseEntity<>(user.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Update user ", description = "This can only be done by the logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<User> updateUserById(@PathVariable("id") long id, @RequestBody User user) {
		Optional<User> userId = userRepository.findById(id);
		if (userId.isPresent()) {
			User userInfo = userId.get();
//			userInfo.setEmail(user.getEmail());
			userInfo.setDepartment(user.getDepartment());
			userInfo.setAffiliation(user.getAffiliation());
			userInfo.setFirstname(user.getFirstname());
			userInfo.setLastname(user.getLastname());
//			userInfo.setPassword(user.getPassword());
			return new ResponseEntity<>(userRepository.save(userInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

//	TODO Reset User's password
//	@Operation(summary = "Update user's password", description = "This can only be done by the logged in user.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "Users" })
//	@ApiResponses(value = @ApiResponse(description = "successful operation"))
//	@PutMapping("/password/{id}")
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//	public ResponseEntity<User> updateUserPasswordById(@PathVariable("id") long id, @RequestBody User user){
//		Optional<User> userId = userRepository.findById(id);
//		if (userId.isPresent()) {
//			User userInfo = userId.get();
//			userInfo.setPassword(encoder.encode(user.getPassword());
////		    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
////			        encoder.encode(signUpRequest.getPassword()));
//
//			return new ResponseEntity<>(userRepository.save(userInfo),HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//		}
//	}

	@Operation(summary = "Delete user", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Users" })
	@ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid user ID supplied"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> deleteUserById(@PathVariable("id") long id) {
		try {
			userRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
