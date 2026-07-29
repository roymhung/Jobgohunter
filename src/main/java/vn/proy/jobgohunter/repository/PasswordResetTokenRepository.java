package vn.proy.jobgohunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.proy.jobgohunter.domain.PasswordResetToken;
import vn.proy.jobgohunter.domain.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);

    void deleteAllByUser(User user);
}
