package com.indux.core.application.service.user;

import com.indux.core.application.dto.user.UserProfileDTO;
import com.indux.core.domain.model.user.UserProfile;
import com.indux.core.domain.repository.user.UserProfileRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.model.auth.User;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileServiceImpl")
class UserProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private UserProfileServiceImpl service;

    private UUID userId;
    private User user;
    private UserProfile profile;
    private UserProfileDTO dto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        
        profile = new UserProfile();
        profile.setUser(user);
        
        dto = new UserProfileDTO((com.indux.core.application.dto.user.SimpleUser) null, "desc", "insta", "twitt", "face");
    }

    @Test
    @DisplayName("Should throw NotFoundEmployee when user not found on getProfile")
    void shouldThrowNotFoundOnGetProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.getProfile(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update profile successfully when user exists")
    void shouldUpdateProfileSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(profile);

        UserProfile result = service.updateProfile(userId, dto);

        assertNotNull(result);
        assertEquals("desc", profile.getDescription());
        assertEquals("insta", profile.getInstagram());
        assertEquals("twitt", profile.getTwitter());
        assertEquals("face", profile.getFacebook());
        verify(profileRepository).save(profile);
    }

    @Test
    @DisplayName("Should throw NotFoundEmployee when user not found on updateProfile")
    void shouldThrowNotFoundOnUpdateProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.updateProfile(userId, dto));
        verify(profileRepository, never()).save(any());
    }
}
