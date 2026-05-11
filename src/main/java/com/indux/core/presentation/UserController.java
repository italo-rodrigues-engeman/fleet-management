package com.indux.core.presentation;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.user.*;
import com.indux.core.domain.model.user.UserProfile;
import com.indux.core.domain.service.user.FavoriteModuleService;
import com.indux.core.domain.service.user.UserProfileService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final FavoriteModuleService favoriteService;

    public UserController(UserService userService, UserProfileService userProfileService,
                          FavoriteModuleService favoriteService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.favoriteService = favoriteService;
    }

    //-------------- ADMINISTRATOR PERMISSION -------------------//
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_DESENVOLVEDOR') or @moduleService.isGerenteOfAnyModule(#jwt.name)")
//   TODO: Alterar para paginação.
    @GetMapping("/all")
    public ResponseEntity<Page<SimpleUser>> getAllUsers(
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC)Pageable pageable,
            UserFilter filter
    ) {
        return ResponseEntity.ok(userService.getFilteredUsers(filter, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_DESENVOLVEDOR')")
    @PutMapping("/{userId}/disable")
    public ResponseEntity<GenericMessage> disableUser(@PathVariable UUID userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok(new GenericMessage("Usuário desativado com sucesso", 200));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_DESENVOLVEDOR')")
    @PutMapping("/{userId}/enable")
    public ResponseEntity<GenericMessage> enableUser(@PathVariable UUID userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok(new GenericMessage("Usuário ativado com sucesso", 200));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_DESENVOLVEDOR')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<GenericMessage> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new GenericMessage("Usuário removido com sucesso", 200));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'ROLE_DESENVOLVEDOR')")
    @PutMapping("/role")
    public ResponseEntity<GenericMessage> updateRoleUser(
            @RequestParam(name = "user") String user,
            @RequestParam(name = "role") Long role) {
        userService.updateRoleUser(UUID.fromString(user), role);
        return ResponseEntity.ok(new GenericMessage("Atualização no cargo do usuário foi realizada com sucesso.", 200));
    }

    //-----------------------ALL---------//
    @GetMapping("/id/{id}")
    public ResponseEntity<SimpleUser> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado.")));
    }

    @GetMapping("/search/{filter}")
    public ResponseEntity<Page<SimpleUser>> searchUser(
            @PathVariable String filter,
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(pageable, filter));
    }

    @GetMapping("/list-basic")
    public ResponseEntity<Page<UserNameProjection>> getBasicUsers(
            @RequestParam(required = false) String nome,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getBasicUsers(nome, pageable));
    }

    @GetMapping("/count")
    public ResponseEntity<SystemUserInfoDTO> countAllUsers() {
        return ResponseEntity.ok(userService.getSystemInfo());
    }

    @GetMapping("/")
    public ResponseEntity<SimpleUser> getCurrentUser(JwtAuthenticationToken jwt) {
        SimpleUser user = userService.getUserById(jwt.getName()).orElseThrow();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<SimpleUser> getProfileFromAuthenticateUser(JwtAuthenticationToken jwt) {
        SimpleUser user = userProfileService.getProfile(UUID.fromString(jwt.getName()));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<SimpleUser> getProfileFromUserID(@PathVariable String id) {
        try {
            // Validar se o ID é um UUID válido
            UUID userUUID = UUID.fromString(id);
            SimpleUser user = userProfileService.getProfile(userUUID);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            throw new NotFoundEmployee("ID do usuário inválido: " + id + ". Deve ser um UUID válido.");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(
            @RequestBody UserProfileDTO profile,
            JwtAuthenticationToken jwtAuthenticationToken) {
        return ResponseEntity
                .ok(userProfileService.updateProfile(UUID.fromString(jwtAuthenticationToken.getName()), profile));
    }

    @PostMapping("/favorite/add/{id}")
    public ResponseEntity<GenericMessage> favoriteModule(
            @PathVariable String id,
            JwtAuthenticationToken token) {
        try {
            if (id.length() < 33)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GenericMessage("Não foi possível identificar esse módulo", 400));
            favoriteService.addFavorite(UUID.fromString(token.getName()), UUID.fromString(id));
            return ResponseEntity.ok(new GenericMessage("Módulo adicionado aos favoritos.", 200));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new GenericMessage("ID do módulo inválido: " + id + ". Deve ser um UUID válido.", 400));
        }
    }

    @PostMapping("/favorite/remove/{id}")
    public ResponseEntity<GenericMessage> removeFavoriteModule(
            @PathVariable String id,
            JwtAuthenticationToken token) {
        try {
            if (id.length() < 33)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new GenericMessage("Não foi possível identificar esse módulo", 400));
            favoriteService.removeFavorite(UUID.fromString(token.getName()), UUID.fromString(id));
            return ResponseEntity.ok(new GenericMessage("Módulo removido dos favoritos.", 200));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new GenericMessage("ID do módulo inválido: " + id + ". Deve ser um UUID válido.", 400));
        }
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericMessage> updateProfileImage(
            @RequestParam("image") MultipartFile image,
            JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getName());
        return ResponseEntity.ok(
                new GenericMessage(userService.updateProfileImageUser(UUID.fromString(token.getName()), image), 200));
    }

    @GetMapping("/me/info")
    public ResponseEntity<EmployeeDTO> getEmployeeInfoByAuthenticatedUser(JwtAuthenticationToken jwt) {
        EmployeeDTO employee = userService.getEmployeeFromUser(UUID.fromString(jwt.getName()));
        return ResponseEntity.ok(employee);
    }

}
