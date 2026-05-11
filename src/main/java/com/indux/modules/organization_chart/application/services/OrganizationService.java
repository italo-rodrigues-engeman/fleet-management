package com.indux.modules.organization_chart.application.services;

import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.dtos.CreateOrganizationDTO;
import com.indux.modules.organization_chart.application.dtos.OrganizationDTO;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
import com.indux.modules.organization_chart.infra.mapper.OrganizationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;
    private final EmployeeRepository employeeRepository;
    private final OrganizationProjectRepository organizationProjectRepository;
    private final OrganizationProjectRepository organizationProjectProjectRepository;

    public OrganizationService(OrganizationRepository organizationRepository, OrganizationMapper mapper, EmployeeRepository employeeRepository, OrganizationProjectRepository organizationProjectRepository, OrganizationProjectRepository organizationProjectProjectRepository) {
        this.organizationRepository = organizationRepository;
        this.mapper = mapper;
        this.employeeRepository = employeeRepository;
        this.organizationProjectRepository = organizationProjectRepository;
        this.organizationProjectProjectRepository = organizationProjectProjectRepository;
    }


    public void createOrganization(CreateOrganizationDTO dto) {
        validate(dto);
        var subordinate = organizationRepository.findById(dto.subordinado()).orElseThrow(() -> new ModuleNotFoundFailure("Subordinado não existe."));
        var employye = employeeRepository.findById(dto.colaborador()).orElseThrow(() -> new ModuleNotFoundFailure("Colaborador não existe."));
        var organization = mapper.toEntity(dto, subordinate, employye);
        organizationRepository.save(organization);
    }

    private void validate(CreateOrganizationDTO dto) {
        if (dto.sigla().length() > 10) throw new ModuleFailure("Sigla precisa ter menos que 10 caracteres");
    }

    public Page<OrganizationDTO> getAllOrganizations(Pageable pageable) {
        return organizationRepository.findAllBy(pageable);
    }

    public Page<OrganizationDTO> getTypeOrganizations(Pageable pageable, OrganizationType type, String search,
            List<Long> subordinadoId) {
        String searchTerm = (search != null && !search.isBlank()) ? search : null;
        Page<OrganizationEntity> organization = organizationRepository.findAllByTypeWithFilters(
                pageable, type, searchTerm, subordinadoId);
        List<OrganizationDTO> dtos = organization.stream()
                .map(mapper::toDtoSingle)
                .toList();
        return new PageImpl<>(dtos, pageable, organization.getTotalElements());
    }

    public void updateOrganization(Long id, CreateOrganizationDTO dto) {
        var organization = organizationRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Organograma não existe."));
        OrganizationEntity subordinate;
        subordinate = validateSubordinate(dto, organization);
        Employee employee;
        employee = validateColaborator(dto, organization);

        if (dto.sigla().length() > 10) throw new ModuleFailure("Sigla precisa ter menos que 10 caracteres");
        var newOrganization = mapper.toEntity(dto, subordinate, employee);
        newOrganization.setId(organization.getId());
        organizationRepository.save(newOrganization);
    }

    private Employee validateColaborator(CreateOrganizationDTO dto, OrganizationEntity organization) {
        Employee employee;
        if (dto.colaborador() == null) {
            employee = organization.getCollaborator();
        } else {
            employee = employeeRepository.findById(dto.colaborador()).orElseThrow(() -> new ModuleNotFoundFailure("Colaborador não existe."));
        }
        return employee;
    }

    private OrganizationEntity validateSubordinate(CreateOrganizationDTO dto, OrganizationEntity organization) {
        OrganizationEntity subordinate;
        if (dto.subordinado() == null) {
            subordinate = organization.getSubordinate();
        } else {
            subordinate = organizationRepository.findById(dto.subordinado()).orElseThrow(() -> new ModuleNotFoundFailure("Subordinado não existe."));
        }
        return subordinate;
    }

    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) throw new ModuleFailure("Organização não encontrado");
        deletValided(id);
        organizationRepository.deleteById(id);
    }

    private void deletValided(Long id) {
        var subordinate = organizationRepository.findBySubordinateId(id);
        if (!subordinate.isEmpty()) throw new ModuleFailure("Colaborador está vinculado a um ou mais colaboradores.");
        var projects = organizationProjectProjectRepository.findBySubordinateId(id);
        if (!projects.isEmpty()) throw new ModuleFailure("Colaborador está vinculado a um ou mais projetos.");
    }

}
