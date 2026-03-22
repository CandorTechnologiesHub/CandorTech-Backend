package com.candortech.repository;

import com.candortech.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findByEmail(String username);
}