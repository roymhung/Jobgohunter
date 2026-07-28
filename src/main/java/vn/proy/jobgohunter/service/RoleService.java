package vn.proy.jobgohunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.Permission;
import vn.proy.jobgohunter.domain.Role;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.repository.PermissionRepository;
import vn.proy.jobgohunter.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;


    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role fetchById(long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent())
            return roleOptional.get();
        return null;
    }

    public Role create(Role r) {
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions().stream()
                    .filter(p -> p.getId() > 0)
                    .map(Permission::getId)
                    .collect(Collectors.toList());

            if (!reqPermissions.isEmpty()) {
                List<Permission> dbPermissions =
                        this.permissionRepository.findByIdIn(reqPermissions);
                r.setPermissions(dbPermissions);
            } else {
                r.setPermissions(List.of());
            }
        }

        return this.roleRepository.save(r);
    }

    public Role update(Role r) {
        if (r.getId() <= 0) {
            return null;
        }
        Role roleDB = this.fetchById(r.getId());
        if (roleDB == null) {
            return null;
        }
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions().stream()
                    .filter(p -> p.getId() > 0)
                    .map(Permission::getId)
                    .collect(Collectors.toList());

            if (!reqPermissions.isEmpty()) {
                List<Permission> dbPermissions =
                        this.permissionRepository.findByIdIn(reqPermissions);
                r.setPermissions(dbPermissions);
            } else {
                r.setPermissions(List.of());
            }
        }

        roleDB.setName(r.getName());
        roleDB.setDescription(r.getDescription());
        roleDB.setActive(r.isActive());
        roleDB.setPermissions(r.getPermissions());
        roleDB = this.roleRepository.save(roleDB);
        return roleDB;
    }


    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO getRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pRole.getTotalPages());
        mt.setTotal(pRole.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pRole.getContent());
        return rs;
    }

}
