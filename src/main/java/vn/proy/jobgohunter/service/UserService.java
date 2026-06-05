package vn.proy.jobgohunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.dto.Meta;
import vn.proy.jobgohunter.domain.dto.ResCreateUserDTO;
import vn.proy.jobgohunter.domain.dto.ResUpdateUserDTO;
import vn.proy.jobgohunter.domain.dto.ResUserDTO;
import vn.proy.jobgohunter.domain.dto.ResultPaginationDTO;
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

    // public List<User> fetchAllUsers(Pageable pageable) {
    // // Logic to fetch all users
    // Page<User> pageUser = this.userRepository.findAll(pageable);
    // return pageUser.getContent();
    // }

    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        // Logic to fetch all users with pagination
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        // remove sensitive data
        List<ResUserDTO> listUser = pageUser.getContent().stream()
                .map(item -> new ResUserDTO(item.getId(), item.getEmail(), item.getName(),
                        item.getAge(), item.getGender(), item.getAddress(), item.getCreatedAt(),
                        item.getUpdatedAt()))
                .collect(Collectors.toList());

        resultPaginationDTO.setResult(listUser);
        return resultPaginationDTO;
    }

    public User handleUpdateUser(User reqUser) {
        // Logic to update a user by ID
        User currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setName(reqUser.getName());
            currentUser.setAge(reqUser.getAge());
            currentUser.setGender(reqUser.getGender());
            currentUser.setAddress(reqUser.getAddress());

            // Save the updated user to the database
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser; // or throw an exception if user not found
    }

    public User handleGetUserByUsername(String username) {
        // Logic to fetch a user by username
        return this.userRepository.findByEmail(username);
    }

    public boolean checkEmailExist(String email) {
        // Logic to check if an email already exists in the database
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setCreatedAt(user.getCreatedAt());

        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();

        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }
}
