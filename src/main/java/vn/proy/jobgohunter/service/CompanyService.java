package vn.proy.jobgohunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.Company;
import vn.proy.jobgohunter.domain.dto.Meta;
import vn.proy.jobgohunter.domain.dto.ResultPaginationDTO;
import vn.proy.jobgohunter.repository.CompanyRepository;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleCreateCompany(Company reqCompany) {
        return this.companyRepository.save(reqCompany);
    }

    // public List<Company> handleGetAllCompanies() {
    // return this.companyRepository.findAll();
    // }

    public ResultPaginationDTO handleGetAllCompanies(Specification<Company> spec,
            Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageCompany.getTotalPages());
        meta.setTotal(pageCompany.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(pageCompany.getContent());
        return resultPaginationDTO;
    }

    public Company handleUpdateCompany(Company reqCompany) {
        Optional<Company> companyOptional = this.companyRepository.findById(reqCompany.getId());
        if (companyOptional.isPresent()) {
            Company currentCompany = companyOptional.get();
            currentCompany.setLogo(reqCompany.getLogo());
            currentCompany.setName(reqCompany.getName());
            currentCompany.setDescription(reqCompany.getDescription());
            currentCompany.setAddress(reqCompany.getAddress());
            return this.companyRepository.save(currentCompany);
        }
        return null;
    }

    public void handleDeleteCompany(Long id) {
        this.companyRepository.deleteById(id);
    }
}
