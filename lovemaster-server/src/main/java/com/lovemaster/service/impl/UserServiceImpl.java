package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lovemaster.entity.User;
import com.lovemaster.mapper.UserMapper;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findByPairCode(String pairCode) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPairCode, pairCode)
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        ) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        ) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        ) > 0;
    }

    @Override
    public User save(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    public User update(User user) {
        userMapper.updateById(user);
        return user;
    }
}
