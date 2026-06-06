package vn.proy.jobgohunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.proy.jobgohunter.domain.Job;
import vn.proy.jobgohunter.domain.Skill;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.domain.response.job.ResCreateJobDTO;
import vn.proy.jobgohunter.domain.response.job.ResUpdateJobDTO;
import vn.proy.jobgohunter.service.JobService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a jobs")
    public ResponseEntity<ResCreateJobDTO> create(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.jobService.create(job));
    }

    @PutMapping("/jobs")
    @ApiMessage("Update a jobs")
    public ResponseEntity<ResUpdateJobDTO> update(@Valid @RequestBody Job job)
            throws IdInvalidException {

        // check id
        Optional<Job> currentJob = this.jobService.fetchJobById(job.getId());
        if (currentJob == null) {
            throw new IdInvalidException("Job khong ton tai");
        }

        return ResponseEntity.ok().body(this.jobService.update(job));
    }

    @DeleteMapping("/jobs/{id}")
    @ApiMessage("Delete a jobs")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (currentJob == null) {
            throw new IdInvalidException("Job khong ton tai");
        }
        this.jobService.deleteJob(id);
        return ResponseEntity.ok().body(null);
    }


    @GetMapping("/jobs/{id}")
    @ApiMessage("Get a job by id")
    public ResponseEntity<Job> getJob(@PathVariable("id") long id) throws IdInvalidException {
        // check id
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (currentJob == null) {
            throw new IdInvalidException("Job khong ton tai");
        }

        return ResponseEntity.ok().body(currentJob.get());
    }

    @GetMapping("/jobs")
    @ApiMessage("Get job with pagonation")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Job> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.jobService.fetchAll(spec, pageable));
    }


}
