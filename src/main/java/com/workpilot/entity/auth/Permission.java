package com.workpilot.entity.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    MANAGER_READ("management:read"),
    MANAGER_UPDATE("management:update"),
    MANAGER_CREATE("management:create"),
    MANAGER_DELETE("management:delete"),
    QUALITE_READ("qualite:read"),
    QUALITE_UPDATE("qualite:update"),
    QUALITE_DELETE("qualite:create"),
    QUALITE_CREATE("qualite:delete");

    @Getter
    private final String permission;
}
