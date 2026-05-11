package com.indux.core.application.service.user;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.dto.user.UserProfileDTO;
import com.indux.core.domain.model.user.UserProfile;
import com.indux.core.domain.repository.user.UserProfileRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.service.user.UserProfileService;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepo;
    private final UserProfileRepository profileRepo;

    public UserProfileServiceImpl(
            UserRepository userRepo,
            UserProfileRepository profileRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    @Override
    public SimpleUser getProfile(UUID id) {
        return SimpleUser.fromEntity(userRepo.findById(id).orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado.")), false, true);
    }

    @Override
    public UserProfile updateProfile(UUID id, UserProfileDTO dto) {
        var user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundEmployee("Usuário não existe: " + id));
        var profile = profileRepo.findById(id).orElse(new UserProfile());
        profile.setUser(user);
        profile.setDescription(dto.descricao());
        profile.setInstagram(dto.instagram());
        profile.setTwitter(dto.twitter());
        profile.setFacebook(dto.facebook());
        return profileRepo.save(profile);
    }
}
