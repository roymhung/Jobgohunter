package vn.proy.jobgohunter.controller;

import java.util.List;

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
    @GetMapping("/user")
    public List<User> getAllUsers() {
        return this.userService.fetchAllUsers();
    }

    // fetch user by id
    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") long id) {
        return this.userService.fetchUserById(id);
    }

    @PostMapping("/user")
    public User createNewUser(@RequestBody User postNewUser) {

        User newUser = this.userService.handleCreateUser(postNewUser);

        return newUser;
    }

    @PutMapping("/user")
    public User updateUser(@RequestBody User updatedUser) {

        User newUdateUser = this.userService.handleUpdateUser(updatedUser);
        return newUdateUser; // Placeholder return statement
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Long id) {
        this.userService.handleDeleteUser(id);
    }

}
