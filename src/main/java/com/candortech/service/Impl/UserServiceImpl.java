package com.candortech.service.Impl;


import com.candortech.config.security.JwtProvider;
import com.candortech.entity.UserProfile;
import com.candortech.repository.UserRepository;
import com.candortech.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public UserProfile findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return userRepository.findByEmail(email);
    }
}