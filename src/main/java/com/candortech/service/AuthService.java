package com.candortech.service;

import com.candortech.dto.AuthResponse;
import com.candortech.dto.LoginRequest;
import com.candortech.dto.request.UserSignupRequest;
import com.candortech.dto.request.GoogleLoginRequest;

/**
 * Defines authentication-related operations for user account access and session handling.
 * <p>
 * Implementations of this service are responsible for registering users, authenticating
 * login requests, and ending authenticated sessions.
 * </p>
 */
public interface AuthService {

    /**
     * Registers a new user account using the provided sign-up data.
     *
     * @param request - the user registration details
     * @return the authentication response containing the newly created session or token data
     */
    AuthResponse signup(UserSignupRequest request);

    /**
     * Authenticates a user with the provided login credentials.
     *
     * @param request - the login credentials
     * @return the authentication response containing session or token data
     */
    AuthResponse login(LoginRequest request);

    /**
     * Authenticates a user with a Google Single Sign-On token.
     *
     * @param request - the Google login request containing the ID token
     * @return the authentication response containing session or token data
     */
    AuthResponse googleLogin(GoogleLoginRequest request);

    /**
     * Logs out the currently authenticated user and invalidates the active session.
     */
    void logout();
}