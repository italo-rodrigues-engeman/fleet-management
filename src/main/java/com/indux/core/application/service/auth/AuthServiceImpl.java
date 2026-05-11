package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.*;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.module.BatchModulePermissionsDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.application.util.NameEmailGenerator;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.generic.TokenEvent;
import com.indux.core.domain.model.generic.TokenEventType;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.repository.user.UserRoleRepository;
import com.indux.core.domain.service.auth.AuthService;
import com.indux.core.domain.service.auth.TokenEventValidator;
import com.indux.core.domain.service.employee.EmployeeLookupService;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.infra.config.security.TokenProvider;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.exception.user.UserBadCredentials;
import com.indux.core.infra.exception.user.UserDisabledException;
import com.indux.core.infra.exception.user.ValidatePasswordFailure;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository repository;
    private final TokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenEventValidator tokenEvent;
    private final ModuleUserService module;
    private final NotificationService notification;
    private final UserRoleRepository roleRepository;
    private final EmployeeLookupService employeeLookupService;

    public AuthServiceImpl(UserRepository repository, TokenProvider jwtTokenProvider, BCryptPasswordEncoder passwordEncoder, TokenEventValidator tokenEvent, ModuleUserService module, NotificationService notification, UserRoleRepository roleRepository, EmployeeLookupService employeeLookupService) {
        this.repository = repository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tokenEvent = tokenEvent;
        this.module = module;
        this.notification = notification;
        this.roleRepository = roleRepository;
        this.employeeLookupService = employeeLookupService;
    }

    // Os dados do colaborador serão enviados via front-end.
    @Override
    public AuthResponse register(RegisterRequest register) throws MessagingException {
        String cpf = AuthenticationValidator.sanitizeCpf(register.cpf());
        Long telefone = AuthenticationValidator.normalizePhoneNumber(register.telefone());
        String tempPassword = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        UserRole role = roleRepository.findById(register.role() != null ? register.role() : 1L).orElse(new UserRole(1L, "USUÁRIO"));
        User user = new User(
                register.nome().toUpperCase(),
                cpf,
                register.email(),
                passwordEncoder.encode(tempPassword),
                register.isPJ(),
                role,
                telefone
        );
        User newUser = repository.save(user);

        TokenEvent token = tokenEvent.createEvent(newUser.getId(), TokenEventType.Values.FIRST_ACCESS.name());
        if (register.modules() != null) {
            BatchModulePermissionsDTO modulePermission = new BatchModulePermissionsDTO(
                    newUser.getId(),
                    register.modules()
            );
            module.addPermissionsBatch(modulePermission);
        }
        sendNotification(SimpleUser.fromEntity(newUser), token);
        return new AuthResponse(newUser.getId().toString());
    }

    @Async
    private void sendNotification(SimpleUser to, TokenEvent token) {
        Notification notificationValue = new Notification();
        notificationValue.setTo(to);
        notificationValue.setTemplateName(TokenEventType.Values.FIRST_ACCESS.name());
        notificationValue.setMessage(token.getToken().toString());
        notificationValue.setVariables(Map.of(
                "expiration", "30 dias"
        ));
        notification.sendToAll(notificationValue, Set.of(NotificationType.EMAIL, NotificationType.WHATSAPP));
    }

    @Override
    public AuthResponse login(LoginRequest login) {
        String cpf = AuthenticationValidator.sanitizeCpf(login.cpf());
        User user = repository.findByCpf(login.cpf());
        if (user == null) throw new NotFoundEmployee("Usuário não encontrado.");
        boolean correctLogin = user.isCorretLogin(login.password(), passwordEncoder);
        
        if (!correctLogin) throw new UserBadCredentials("Credênciais inválidas.");
        if (user.isDisable()) throw new UserDisabledException("Usuário desativado");

        String jwtToken = jwtTokenProvider.generateToken(user);
        user.setLastLogin(Instant.now());
        repository.save(user);
        return new AuthResponse(jwtToken);
    }

    @Override
    public GenericMessage changePassword(ChangePasswordDTO request) {
        // Validações sem necessidade chamar o banco de dados
        validate(request);

        User user = repository.findByCpf(request.cpf());
        if (user == null) throw new NotFoundEmployee("Usuário não encontrado.");
        if (!user.isCorretLogin(request.oldPassword(), passwordEncoder))
            throw new UserBadCredentials("A senha antiga está incorreta.");

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        repository.save(user);
        return new GenericMessage("Senha alterada com sucesso", 200);
    }

    @Override
    @Transactional
    public RegisterBatchResponse registerBatch(RegisterBatchRequest request) {
        validateBatchRequest(request);

        List<CreatedUserItem> created = new ArrayList<>();
        List<SkippedUserItem> skipped = new ArrayList<>();

        List<String> validMatriculas = request.matriculas().stream()
                .filter(m -> m != null && !m.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        List<Employee> employees = employeeLookupService.findByRegistrations(validMatriculas);
        Map<String, Employee> employeeByMatricula = employees.stream()
                .collect(Collectors.toMap(Employee::getRegistration, e -> e, (e1, e2) -> e1));

        Set<String> cpfsToCheck = employees.stream()
                .map(Employee::getCpf)
                .filter(cpf -> cpf != null && !cpf.isBlank())
                .collect(Collectors.toSet());

        Set<String> existingCpfs = new HashSet<>();
        if (!cpfsToCheck.isEmpty()) {
            existingCpfs = cpfsToCheck.stream()
                    .map(cpf -> {
                        User existing = repository.findByCpf(cpf);
                        return existing != null ? cpf : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        UserRole defaultRole = roleRepository.findById(1L).orElse(new UserRole(1L, "USUÁRIO"));
        String encodedPassword = passwordEncoder.encode(request.senha());

        List<User> usersToCreate = new ArrayList<>();
        Map<String, String> matriculaToUserId = new HashMap<>();

        for (String matricula : validMatriculas) {
            Employee employee = employeeByMatricula.get(matricula);

            if (employee == null) {
                skipped.add(new SkippedUserItem(matricula, SkippedUserItem.SkipReason.NOT_FOUND));
                continue;
            }

            String cpf = employee.getCpf();
            if (cpf == null || cpf.isBlank()) {
                skipped.add(new SkippedUserItem(matricula, SkippedUserItem.SkipReason.INVALID_DATA));
                continue;
            }

            String sanitizedCpf = AuthenticationValidator.sanitizeCpf(cpf);
            if (existingCpfs.contains(sanitizedCpf)) {
                skipped.add(new SkippedUserItem(matricula, SkippedUserItem.SkipReason.ALREADY_EXISTS));
                continue;
            }

            String email = NameEmailGenerator.generateEmail(employee.getName());
            if (email == null || email.isBlank()) {
                skipped.add(new SkippedUserItem(matricula, SkippedUserItem.SkipReason.INVALID_DATA));
                continue;
            }

            Long telefone = extractPhoneNumber(employee);

            User user = new User(
                    employee.getName().toUpperCase(),
                    sanitizedCpf,
                    email,
                    encodedPassword,
                    false,
                    defaultRole,
                    telefone
            );

            usersToCreate.add(user);
            matriculaToUserId.put(matricula, null);
        }

        if (!usersToCreate.isEmpty()) {
            List<User> savedUsers = repository.saveAll(usersToCreate);
            
            int index = 0;
            for (String matricula : validMatriculas) {
                if (matriculaToUserId.containsKey(matricula) && matriculaToUserId.get(matricula) == null) {
                    User savedUser = savedUsers.get(index);
                    matriculaToUserId.put(matricula, savedUser.getId().toString());
                    created.add(new CreatedUserItem(
                            matricula,
                            savedUser.getId().toString(),
                            savedUser.getEmail()
                    ));
                    index++;
                }
            }
        }

        return new RegisterBatchResponse(created, skipped);
    }

    private void validateBatchRequest(RegisterBatchRequest request) {
        if (request == null) {
            throw new ValidatePasswordFailure("Request não pode ser nulo");
        }
        if (request.matriculas() == null || request.matriculas().isEmpty()) {
            throw new ValidatePasswordFailure("Lista de matrículas não pode estar vazia");
        }
        if (request.senha() == null || request.senha().isBlank()) {
            throw new ValidatePasswordFailure("Senha não pode estar vazia");
        }
//        List<String> failures = AuthenticationValidator.validatePassword(request.senha());
//        if (!failures.isEmpty()) {
//            throw new ValidatePasswordFailure(String.join(", ", failures));
//        }
    }

    private Long extractPhoneNumber(Employee employee) {
        String phone = employee.getCellphone();
        if (phone != null && !phone.isBlank()) {
            Long normalized = AuthenticationValidator.normalizePhoneNumber(phone);
            if (normalized != null && normalized > 0L) {
                return normalized;
            }
        }
        
        String phone2 = employee.getCellphone2();
        if (phone2 != null && !phone2.isBlank()) {
            Long normalized = AuthenticationValidator.normalizePhoneNumber(phone2);
            if (normalized != null && normalized > 0L) {
                return normalized;
            }
        }
        
        return 0L;
    }

    private void validate(ChangePasswordDTO request) {
        if (!(request.newPassword().equals(request.confirmPassword())))
            throw new UserBadCredentials("As senhas não condizem");
        List<String> failures = AuthenticationValidator.validatePassword(request.newPassword());
        if (!failures.isEmpty()) throw new ValidatePasswordFailure(failures.stream().toString());
    }
}
