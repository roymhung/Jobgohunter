package vn.proy.jobgohunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.proy.jobgohunter.domain.Job;
import vn.proy.jobgohunter.domain.Skill;


@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findBySkillsIn(List<Skill> skills);

    @Query("SELECT DISTINCT j FROM Job j JOIN j.skills s WHERE s.id IN :skillIds AND j.active = true")
    List<Job> findDistinctBySkillIds(@Param("skillIds") List<Long> skillIds);

}
