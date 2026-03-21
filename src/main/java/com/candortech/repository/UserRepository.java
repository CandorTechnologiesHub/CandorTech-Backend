package com.candortech.repository;

import com.candortech.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByEmail(String username);
}