package vn.proy.jobgohunter.controller;

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
import vn.proy.jobgohunter.domain.Company;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.service.CompanyService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseEntity<?> createCompany(@Valid @RequestBody Company reqCompany) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.companyService.handleCreateCompany(reqCompany));
    }

    @GetMapping("/companies")
    @ApiMessage("Fetch all companies with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO> getCompany(@Filter Specification<Company> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.companyService.handleGetAllCompanies(spec, pageable));
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company reqCompany) {
        Company updatedCompany = this.companyService.handleUpdateCompany(reqCompany);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") Long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
