package com.lovemaster.service;

import com.lovemaster.entity.User;

public interface UserService {

    User findByUsername(String username);

    User findById(Long id);

    User findByPairCode(String pairCode);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    User save(User user);

    User update(User user);
}
