package com.github.bucketonhead.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.bucketonhead.entity.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserCache extends AbstractCaffeineAppCache<Long, AppUser> {

    public AppUserCache(Cache<Long, AppUser> cache) {
        super(cache);
    }

    @Override
    public Long getKey(AppUser appUser) {
        return appUser.getTelegramUserId();
    }
}
