package com.candortech.dto.request;

import com.candortech.entity.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO to be used for User Signup for {@link UserProfile}
 */
public record UserSignupRequest(
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @NotBlank(message = "First name cannot be blank")
        String lastName,

        String otherNames,

        @Email(message = "Must be an Email")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @Pattern(message = "Invalid phone number", regexp = "^\\\\+?[0-9\\\\s]{7,15}$")
        @NotBlank(message = "Phone number cannot be black")
        String phone,

        @Pattern(
                message = "Password must be at least 8 characters and include uppercase, lowercase, number and special character",
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[@$!%*?&])[A-Za-z\\\\d@$!%*?&]{8,}$"
        )
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) implements Serializable {}