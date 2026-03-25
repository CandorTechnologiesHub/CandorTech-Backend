package com.candortech.service;

import com.candortech.entity.UserProfile;

public interface UserService {
    UserProfile findUserByJwtToken(String jwt);
}