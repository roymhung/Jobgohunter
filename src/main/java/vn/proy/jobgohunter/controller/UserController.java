package vn.proy.jobgohunter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.service.UserService;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // fetch all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(this.userService.fetchAllUsers());
    }

    // fetch user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User fetchUser = this.userService.fetchUserById(id);

        return ResponseEntity.status(HttpStatus.OK).body(fetchUser);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User postNewUser) {

        User newUser = this.userService.handleCreateUser(postNewUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/users")
    public User updateUser(@RequestBody User updatedUser) {

        User newUdateUser = this.userService.handleUpdateUser(updatedUser);
        return newUdateUser; // Placeholder return statement
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    // @DeleteMapping("/users/{id}")
    // public void deleteUser(@PathVariable Long id) {
    // this.userService.handleDeleteUser(id);
    // }

    // @DeleteMapping("/users/{id}")
    // public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // this.userService.handleDeleteUser(id);
    // return ResponseEntity.noContent().build();
    // }
}
