package vn.proy.jobgohunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        // Logic to create a user and save to the database
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(Long id) {
        // Logic to delete a user by ID
        this.userRepository.deleteById(id);
    }

    public User fetchUserById(Long id) {
        // Logic to fetch a user by ID
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null; // or throw an exception if user not found
    }

    public List<User> fetchAllUsers() {
        // Logic to fetch all users
        return this.userRepository.findAll();
    }

    public User handleUpdateUser(User reqUser) {
        // Logic to update a user by ID
        User currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setUsername(reqUser.getUsername());
            currentUser.setEmail(reqUser.getEmail());
            currentUser.setPassword(reqUser.getPassword());

            // Save the updated user to the database
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser; // or throw an exception if user not found
    }
}
