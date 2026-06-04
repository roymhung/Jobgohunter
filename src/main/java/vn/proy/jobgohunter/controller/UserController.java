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
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.dto.ResultPaginationDTO;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.util.error.IdInvalidException;


@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // fetch all users
    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.userService.fetchAllUsers(spec, pageable));
    }

    // fetch user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User fetchUser = this.userService.fetchUserById(id);

        return ResponseEntity.status(HttpStatus.OK).body(fetchUser);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User postNewUser) {
        String hashPassword = this.passwordEncoder.encode(postNewUser.getPassword());
        postNewUser.setPassword(hashPassword);
        User newUser = this.userService.handleCreateUser(postNewUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {

        User newUdateUser = this.userService.handleUpdateUser(updatedUser);
        return ResponseEntity.ok(newUdateUser); // Placeholder return statement
    }



    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id)
            throws IdInvalidException {

        if (id > 1500) {
            throw new IdInvalidException("Invalid ID khong lon hon 1500");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

}
