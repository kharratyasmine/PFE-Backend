package com.workpilot.repository.ressources;

import com.workpilot.entity.auth.Role;
import com.workpilot.entity.auth.User;
import com.workpilot.entity.auth.token.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByApprovalStatus(ApprovalStatus status);

}
