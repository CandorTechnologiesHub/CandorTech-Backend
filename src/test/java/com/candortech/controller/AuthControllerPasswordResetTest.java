package com.candortech.controller;

import com.candortech.config.security.AppConfig;
import com.candortech.config.security.JwtProvider;
import com.candortech.exception.ApiException;
import com.candortech.repository.UserRepository;
import com.candortech.service.PasswordResetService;
import com.candortech.service.UserService;
import com.candortech.service.impl.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerPasswordResetTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean PasswordResetService passwordResetService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean UserService userService;
    @MockitoBean CustomUserDetails customUserDetails;
    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean JwtProvider jwtProvider;

    // -------------------------------------------------------------------------
    // POST /auth/forgot-password
    // -------------------------------------------------------------------------

    @Test
    void forgotPassword_validEmail_returns200() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(passwordResetService).initiatePasswordReset("alice@example.com");
    }

    @Test
    void forgotPassword_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void forgotPassword_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    // -------------------------------------------------------------------------
    // POST /auth/reset-password
    // -------------------------------------------------------------------------

    @Test
    void resetPassword_validRequest_returns200() throws Exception {
        String body = """
                {"token":"abc123","newPassword":"newPass1!","confirmPassword":"newPass1!"}
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(passwordResetService).resetPassword("abc123", "newPass1!", "newPass1!");
    }

    @Test
    void resetPassword_passwordTooShort_returns400() throws Exception {
        String body = """
                {"token":"abc123","newPassword":"short","confirmPassword":"short"}
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void resetPassword_missingToken_returns400() throws Exception {
        String body = """
                {"newPassword":"newPass1!","confirmPassword":"newPass1!"}
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void resetPassword_serviceThrowsApiException_returns400() throws Exception {
        doThrow(new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST.value()))
                .when(passwordResetService).resetPassword(any(), any(), any());

        String body = """
                {"token":"abc123","newPassword":"newPass1!","confirmPassword":"different!"}
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_expiredToken_returns400() throws Exception {
        doThrow(new ApiException("Reset token has expired", HttpStatus.BAD_REQUEST.value()))
                .when(passwordResetService).resetPassword(any(), any(), any());

        String body = """
                {"token":"expired-token","newPassword":"newPass1!","confirmPassword":"newPass1!"}
                """;

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
