package vn.proy.jobgohunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.proy.jobgohunter.domain.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}
