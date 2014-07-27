package com.mvmlabs.springboot.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.mvmlabs.springboot.domain.User;

/**
 * Spring Data repository for interacting with user details data store.
 * 
 * @author Mark Meany
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    /**
     * Find a users record given their name.
     * 
     * @param name the name to identify
     * @return
     */
    User findByName(String name);
}
