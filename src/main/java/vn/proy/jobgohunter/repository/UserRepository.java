package vn.proy.jobgohunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.proy.jobgohunter.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
