package com.mj.middleware.redis.until;


import org.springframework.stereotype.Component;


public class UserContext {
    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();
    public static void setCurrentUser(Long userId) {
        currentUser.set(userId);
    }
    public static Long getCurrentUser() {
        return currentUser.get();
    }
    public static void clear() {
        currentUser.remove();
    }
}
