package com.mvmlabs.springboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mvmlabs.springboot.dao.UserRepository;
import com.mvmlabs.springboot.domain.User;
import com.mvmlabs.springboot.service.UserService;

@Service
@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
public class UserServiceJpaImpl implements UserService {

    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceJpaImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public User registerVisit(final String name) {
        
        User user = userRepository.findByName(name);
        if (user == null) {
            user = new User(name);
        }
        user.setNumberOfVisits(user.getNumberOfVisits() + 1);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<User> getAllRegisteredUsers(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
