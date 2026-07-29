package vn.proy.jobgohunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.util.enums.AuthProvider;

import java.util.List;

import vn.proy.jobgohunter.domain.Company;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

    User findByAuthProviderAndProviderId(AuthProvider authProvider, String providerId);

    List<User> findByCompany(Company company);
}
