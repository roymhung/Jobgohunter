package vn.proy.jobgohunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.Company;
import vn.proy.jobgohunter.domain.Job;
import vn.proy.jobgohunter.domain.Skill;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.domain.response.job.ResCreateJobDTO;
import vn.proy.jobgohunter.domain.response.job.ResUpdateJobDTO;
import vn.proy.jobgohunter.repository.CompanyRepository;
import vn.proy.jobgohunter.repository.JobRepository;
import vn.proy.jobgohunter.repository.SkillRepository;


@Service
public class JobService {

    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;


    public JobService(SkillRepository skillRepository, JobRepository jobRepository,
            CompanyRepository companyRepository) {
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Job> fetchJobById(long id) {
        return this.jobRepository.findById(id);
    }

    public ResCreateJobDTO create(Job j) {
        // check skills
        if (j.getSkills() != null) {
            List<Long> reqSkills =
                    j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            j.setSkills(dbSkills);
        }

        // check company
        if (j.getCompany() != null) {

            Optional<Company> cOptional = this.companyRepository.findById(j.getCompany().getId());

            if (cOptional.isPresent()) {
                j.setCompany(cOptional.get());
            }
        }

        // create job
        Job currentJob = this.jobRepository.save(j);

        // convert response
        ResCreateJobDTO dto = new ResCreateJobDTO();

        dto.setId(currentJob.getId());
        dto.setName(currentJob.getName());
        dto.setSalary(currentJob.getSalary());
        dto.setQuantity(currentJob.getQuantity());
        dto.setLocation(currentJob.getLocation());
        dto.setLevel(currentJob.getLevel());
        dto.setStartDate(currentJob.getStartDate());
        dto.setEndDate(currentJob.getEndDate());
        dto.setActive(currentJob.isActive());
        dto.setCreatedAt(currentJob.getCreatedAt());
        dto.setCreatedBy(currentJob.getCreatedBy());

        if (currentJob.getSkills() != null) {
            List<String> skills = currentJob.getSkills().stream().map(item -> item.getName())
                    .collect(Collectors.toList());

            dto.setSkills(skills);
        }

        return dto;
    }


    public ResUpdateJobDTO update(Job j, Job jobInDB) {
        // check skills
        if (j.getSkills() != null) {
            List<Long> reqSkills =
                    j.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            jobInDB.setSkills(dbSkills);
        }

        // check company
        if (j.getCompany() != null) {

            Optional<Company> cOptional = this.companyRepository.findById(j.getCompany().getId());

            if (cOptional.isPresent()) {
                jobInDB.setCompany(cOptional.get());
            }
        }

        // convert response
        ResUpdateJobDTO dto = new ResUpdateJobDTO();

        // update correct info
        jobInDB.setName(j.getName());
        jobInDB.setSalary(j.getSalary());
        jobInDB.setQuantity(j.getQuantity());
        jobInDB.setLocation(j.getLocation());
        jobInDB.setLevel(j.getLevel());
        jobInDB.setStartDate(j.getStartDate());
        jobInDB.setEndDate(j.getEndDate());
        jobInDB.setActive(j.isActive());

        // update job
        Job currentJob = this.jobRepository.save(jobInDB);

        if (currentJob.getSkills() != null) {
            List<String> skills = currentJob.getSkills().stream().map(item -> item.getName())
                    .collect(Collectors.toList());

            dto.setSkills(skills);
        }

        return dto;
    }

    public void deleteJob(long id) {
        this.jobRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = this.jobRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageJob.getTotalPages());
        meta.setTotal(pageJob.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageJob.getContent());
        return resultPaginationDTO;
    }



}
