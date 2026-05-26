package vn.proy.jobgohunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.service.UserService;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/create")
    public String createNewUser() {

        User user = new User("john_doe", "john.doe@example.com", "123456");
        this.userService.handleCreateUser(user);

        return "User created successfully!";
    }

}
