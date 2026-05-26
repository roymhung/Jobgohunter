package vn.proy.jobgohunter.service;

import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void handleCreateUser(User user) {
        // Logic to create a user and save to the database
        this.userRepository.save(user);
    }

}
