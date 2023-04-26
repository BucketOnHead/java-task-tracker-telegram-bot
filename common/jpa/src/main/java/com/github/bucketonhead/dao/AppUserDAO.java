package com.github.bucketonhead.dao;

import com.github.bucketonhead.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    AppUser findByTelegramUserId(Long id);
}