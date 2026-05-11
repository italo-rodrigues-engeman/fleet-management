package com.indux.core.application.service.user;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.dto.user.SystemUserInfoDTO;
import com.indux.core.application.dto.user.UserFilter;
import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.repository.user.UserRoleRepository;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserRoleRepository roleRepository;
    private final StorageService storage;
    private final ModuleUserService moduleUserService;
    private final GetEmployeeUseCase getEmployeeUseCase;

    public UserServiceImpl(UserRepository repository, UserRoleRepository roleRepository, StorageService storage, ModuleUserService moduleUserService, GetEmployeeUseCase getEmployeeUseCase) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.storage = storage;
        this.moduleUserService = moduleUserService;
        this.getEmployeeUseCase = getEmployeeUseCase;
    }

    @Override
    public List<SimpleUser> getAllUsers() {
        return repository.findAll().stream()
                .map(SimpleUser::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SimpleUser> getUserById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(SimpleUser::fromEntity);
    }

    @Override
    public Page<SimpleUser> searchUsers(Pageable pageable, String filter) {
        if (filter == null || filter.isBlank()) {
            return Page.empty();
        }

        Page<User> users;
        String cleaned = filter.replaceAll("\\D", "");
        if (cleaned.matches("\\d{11}")) {
            User user = repository.findByCpf(cleaned);
            List<User> usersList = user != null ? List.of(user) : List.of();
            users = new PageImpl<>(usersList);
        } else if (filter.contains("@")) {
            users = repository.findAllByEmailContainingIgnoreCase(pageable, filter.trim());
        } else {
            users = repository.findAllByNameContainingIgnoreCase(pageable, filter.trim());
        }
        return users.map(SimpleUser::fromEntity);
    }

    @Override
    public Page<SimpleUser> getFilteredUsers(UserFilter filter, Pageable pageable) {
        Page<User> userPage;

        if (filter.role() != null && filter.isDisable() != null) {
            userPage = repository.findByRoleAndIsDisable(filter.role(), filter.isDisable(), pageable);
        } else if (filter.role() != null) {
            userPage = repository.findByRole(pageable, filter.role());
        } else if (filter.isDisable() != null) {
            userPage = repository.findByIsDisable(pageable, filter.isDisable());
        } else {
            userPage = repository.findAll(pageable);
        }

        return userPage.map(SimpleUser::fromEntity);
    }

    @Override
    public User getUserByCPF(String cpf) {
        return repository.findByCpf(cpf);
    }

    @Override
    public long countUsers() {
        return repository.count();
    }

    @Override
    public SystemUserInfoDTO getSystemInfo() {
        List<String> ultimosCadastrados = repository.findLast10UserNames();
        long total = repository.count();
        long desativados = repository.countByIsDisableTrue();

        return new SystemUserInfoDTO(total, desativados, ultimosCadastrados);
    }

    @Override
    public void updateRoleUser(UUID id, long roleID) {
        UserRole role = roleRepository.findById(roleID).orElse(null);
        if (role == null)
            throw new IllegalArgumentException("Não é possível alterar para um cargo inexistente.");
        User currentUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundEmployee("Não foi possível encontrar o usuário."));
        currentUser.setRole(role);
        repository.save(currentUser);
    }

    @Override
    public void disableUser(UUID id) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
        user.setDisable(true);
        user.setUpdatedAt(Instant.now());
        repository.save(user);
    }

    @Override
    public void enableUser(UUID id) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
        user.setDisable(false);
        user.setUpdatedAt(Instant.now());
        repository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));

        moduleUserService.removeUserFromAllModules(user.getId());

        repository.delete(user);
    }

    @Override
    public String updateProfileImageUser(UUID user, MultipartFile file) {
        User editedUser = repository.findById(user).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado"));
        Path metadata = storage.store(file, "user/profile", editedUser.getId().toString());
        String uri = storage.getRootLocation().relativize(metadata).toString().replace("\\", "/");

        Path thumbPath = storage.createThumbnail(
                "user/profile/" + metadata.getFileName().toString(),
                150, 150
        );
        String thumbUri = storage.getRootLocation()
                .relativize(thumbPath)
                .toString()
                .replace("\\", "/");
        editedUser.setPhotoUrl(uri);
        editedUser.setThumbPhotoUrl(thumbUri);
        editedUser.setUpdatedAt(Instant.now());
        repository.save(editedUser);
        return uri;
    }

    @Override
    public EmployeeDTO getEmployeeFromUser(UUID user) {
        User data = repository.findById(user).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
        EmployeeDTO employeeDTO = getEmployeeUseCase.getActiveEmployeeByCPF(data.getCpf());
        if (employeeDTO == null) {
            throw new NotFoundEmployee("Funcionário não encontrado para o usuário.");
        }
        return employeeDTO;
    }

    @Override
    public Employee getCompleteEmployeeFromUser(UUID user) {
        User data = repository.findById(user).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
        Employee employee = getEmployeeUseCase.getCompleteActiveEmployeeByCPF(data.getCpf());
        return employee;
    }

    @Override
    public List<UserNameProjection> getUserNamesIn(List<UUID> ids){
        return repository.findAllByIdIn(ids);
    }

    @Override
    public Page<UserNameProjection> getBasicUsers(String nameFilter, Pageable pageable) {
        if (nameFilter != null && !nameFilter.isBlank()) {
            return repository.findByNameContainingIgnoreCase(nameFilter, pageable);
        }
        return repository.findAllProjectedBy(pageable);
    }
}
