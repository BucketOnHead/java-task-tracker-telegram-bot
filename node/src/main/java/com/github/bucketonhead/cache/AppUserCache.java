package com.github.bucketonhead.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.bucketonhead.entity.user.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppUserCache implements AppCache<Long, AppUser> {
    private final Cache<Long, AppUser> cache;

    @Override
    public Optional<AppUser> get(Long tgUserId) {
        AppUser cachedAppUser = cache.getIfPresent(tgUserId);
        if (cachedAppUser != null) {
            log.info("User found in cache: tg_user_id={}", tgUserId);
            log.debug("User found in cache: {}", cachedAppUser);
        } else {
            log.info("User not found in cache: tg_user_id={}", tgUserId);
        }

        return Optional.ofNullable(cachedAppUser);
    }

    @Override
    public void put(AppUser appUser) {
        cache.put(appUser.getTelegramUserId(), appUser);
        log.info("User added to cache: tg_user_id={}", appUser.getTelegramUserId());
        log.debug("User added to cache: {}", appUser);
    }

    @Override
    public boolean contains(Long tgUserId) {
        boolean contains = cache.getIfPresent(tgUserId) != null;
        if (contains) {
            log.info("User found in cache: tg_user_id={}", tgUserId);
        } else {
            log.info("User not found in cache: tg_user_id={}", tgUserId);
        }

        return contains;
    }

    @Override
    public void remove(Long tgUserId) {
        cache.invalidate(tgUserId);
        log.info("User removed from cache: tg_user_id={}", tgUserId);
    }
}
