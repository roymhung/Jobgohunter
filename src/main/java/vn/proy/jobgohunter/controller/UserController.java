package vn.proy.jobgohunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.response.ResCreateUserDTO;
import vn.proy.jobgohunter.domain.response.ResUpdateUserDTO;
import vn.proy.jobgohunter.domain.response.ResUserDTO;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // fetch all users
    @GetMapping("/users")
    @ApiMessage("Fetch all users with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.userService.fetchAllUsers(spec, pageable));
    }

    // fetch user by id
    @GetMapping("/users/{id}")
    @ApiMessage("Fetch a user by ID")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id)
            throws IdInvalidException {

        User fetchUser = this.userService.fetchUserById(id);

        if (fetchUser == null) {
            throw new IdInvalidException("User with ID " + id + " not found");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.convertToResUserDTO(fetchUser));
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@RequestBody User user)
            throws IdInvalidException {

        boolean isEmailExist = this.userService.checkEmailExist(user.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email" + user.getEmail() + " already exists, please choose another email!");
        }

        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User newUser = this.userService.handleCreateUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResCreateUserDTO(newUser));
    }

    @PutMapping("/users")
    @ApiMessage("Update a user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user)
            throws IdInvalidException {

        User newUdateUser = this.userService.handleUpdateUser(user);

        if (newUdateUser == null) {
            throw new IdInvalidException("User with ID " + user.getId() + " not found");
        }

        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(newUdateUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {

        User currentUser = this.userService.fetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User with ID " + id + " not found");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
