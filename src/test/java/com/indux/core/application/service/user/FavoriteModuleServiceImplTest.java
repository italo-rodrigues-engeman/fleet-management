package com.indux.core.application.service.user;

import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.user.UserFavoriteModule;
import com.indux.core.domain.model.user.UserModuleKey;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.repository.user.UserFavoriteModuleRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteModuleServiceImpl")
class FavoriteModuleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private UserFavoriteModuleRepository favoriteModuleRepository;

    @Mock
    private ModuleResponseMapper moduleResponseMapper;

    @InjectMocks
    private FavoriteModuleServiceImpl favoriteModuleService;

    @Nested
    @DisplayName("Fixtures")
    class Fixtures {
        protected final UUID userId = UUID.randomUUID();
        protected final UUID moduleId = UUID.randomUUID();
        protected final User user = new User();
        protected final Modulo module = new Modulo();
        protected final UserModuleKey moduleKey = new UserModuleKey(userId, moduleId);
        protected final UserFavoriteModule favoriteModule = new UserFavoriteModule();
        protected final ModuleResponseDTO moduleResponseDTO = mock(ModuleResponseDTO.class);

        @BeforeEach
        void setUp() {
            user.setId(userId);
            module.setId(moduleId);
            favoriteModule.setId(moduleKey);
            favoriteModule.setUser(user);
            favoriteModule.setModule(module);
        }
    }

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite extends Fixtures {

        @Test
        @DisplayName("Should add favorite when not exists")
        void shouldAddFavoriteWhenNotExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
            when(favoriteModuleRepository.existsById(moduleKey)).thenReturn(false);

            favoriteModuleService.addFavorite(userId, moduleId);

            verify(favoriteModuleRepository).save(any(UserFavoriteModule.class));
        }

        @Test
        @DisplayName("Should not add favorite when already exists")
        void shouldNotAddFavoriteWhenAlreadyExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
            when(favoriteModuleRepository.existsById(moduleKey)).thenReturn(true);

            favoriteModuleService.addFavorite(userId, moduleId);

            verify(favoriteModuleRepository, never()).save(any(UserFavoriteModule.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteModuleService.addFavorite(userId, moduleId))
                    .isInstanceOf(NotFoundEmployee.class)
                    .hasMessageContaining("Usuário não existe");
        }

        @Test
        @DisplayName("Should throw exception when module not found")
        void shouldThrowExceptionWhenModuleNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteModuleService.addFavorite(userId, moduleId))
                    .isInstanceOf(ModuleNotFoundFailure.class)
                    .hasMessageContaining("Módulo não existe");
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite extends Fixtures {

        @Test
        @DisplayName("Should remove favorite")
        void shouldRemoveFavorite() {
            favoriteModuleService.removeFavorite(userId, moduleId);

            verify(favoriteModuleRepository).deleteById(moduleKey);
        }
    }

    @Nested
    @DisplayName("removeAllUsersFromModule")
    class RemoveAllUsersFromModule extends Fixtures {

        @Test
        @DisplayName("Should remove all users when module exists")
        void shouldRemoveAllUsersWhenModuleExists() {
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));

            favoriteModuleService.removeAllUsersFromModule(moduleId);

            verify(favoriteModuleRepository).deleteByModuleId(moduleId);
        }

        @Test
        @DisplayName("Should throw exception when module not exists")
        void shouldThrowExceptionWhenModuleNotExists() {
            when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteModuleService.removeAllUsersFromModule(moduleId))
                    .isInstanceOf(ModuleNotFoundFailure.class)
                    .hasMessageContaining("Módulo não existe");
        }
    }

    @Nested
    @DisplayName("listFavorites")
    class ListFavorites extends Fixtures {

        @Test
        @DisplayName("Should return favorite modules")
        void shouldReturnFavoriteModules() {
            List<UserFavoriteModule> favoriteModules = List.of(favoriteModule);
            when(favoriteModuleRepository.findByUserId(userId)).thenReturn(favoriteModules);
            when(moduleResponseMapper.toDTO(module, userId, false, true)).thenReturn(moduleResponseDTO);

            List<ModuleResponseDTO> result = favoriteModuleService.listFavorites(userId);

            assertThat(result).hasSize(1);
            assertThat(result).contains(moduleResponseDTO);
            verify(moduleResponseMapper).toDTO(module, userId, false, true);
        }

        @Test
        @DisplayName("Should return empty list when no favorites")
        void shouldReturnEmptyListWhenNoFavorites() {
            when(favoriteModuleRepository.findByUserId(userId)).thenReturn(List.of());

            List<ModuleResponseDTO> result = favoriteModuleService.listFavorites(userId);

            assertThat(result).isEmpty();
            verify(moduleResponseMapper, never()).toDTO(any(), any(), anyBoolean(), anyBoolean());
        }
    }
}