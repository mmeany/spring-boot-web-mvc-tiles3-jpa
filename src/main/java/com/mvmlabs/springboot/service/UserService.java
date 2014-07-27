package com.mvmlabs.springboot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mvmlabs.springboot.domain.User;

public interface UserService {

    User registerVisit(String name);
    
    Page<User> getAllRegisteredUsers(final Pageable pageable);
}
