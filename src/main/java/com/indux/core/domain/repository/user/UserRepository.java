package com.indux.core.domain.repository.user;

import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByCpf(String cpf);

    User findByName(String name);

    List<UserNameProjection> findAllByIdIn(List<UUID> ids);

    Page<User> findAllByNameContainingIgnoreCase(Pageable pageable, String name);

    Page<User> findAllByEmailContainingIgnoreCase(Pageable pageable, String email);

    Page<UserNameProjection> findAllProjectedBy(Pageable pageable);

    Page<UserNameProjection> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByRoleAndIsDisable(UserRole role, Boolean isDisable, Pageable pageable);

    Page<User> findByRole(Pageable pageable, UserRole role);

    Page<User> findByIsDisable(Pageable pageable, Boolean isDisable);

    List<User> findAllByCpf(String cpf);

    User findByEmail(String email);

    @Query("SELECT u.name FROM User u ORDER BY u.createdAt DESC")
    List<String> findLastUserNames(Pageable pageable);

    default List<String> findLast10UserNames() {
        return findLastUserNames(PageRequest.of(0, 10));
    }

    long countByIsDisableTrue();

}
