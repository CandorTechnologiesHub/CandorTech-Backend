package com.candortech.dto.response;

import com.candortech.entity.UserProfile;
import com.candortech.enums.AccountStatus;
import com.candortech.enums.KycLevel;
import com.candortech.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String otherNames;
    private String email;
    private String phone;
    private UserRole role;
    private KycLevel kycLevel;
    private AccountStatus accountStatus;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(UserProfile user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .otherNames(user.getOtherNames())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .kycLevel(user.getKycLevel())
                .accountStatus(user.getAccountStatus())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .mfaEnabled(user.isMfaEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}