package com.workpilot.entity.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    // ADMIN permissions
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    // RESPONSABLE PROJET
    PROJECT_READ("project:read"),
    PROJECT_UPDATE("project:update"),
    PROJECT_CREATE("project:create"),
    PROJECT_DELETE("project:delete"),


    // RESPONSABLE QUALITE
    RES_QUALITE_READ("resqualite:read"),
    RES_QUALITE_UPDATE("resqualite:update"),
    RES_QUALITE_CREATE("resqualite:create"),
    RES_QUALITE_DELETE("resqualite:delete"),

    // COORDINATEUR QUALITE
    COORD_QUALITE_READ("coordqualite:read"),
    COORD_QUALITE_UPDATE("coordqualite:update"),

    // INGÉNIEUR
    ENGINEER_READ("engineer:read"),
    ENGINEER_UPDATE("engineer:update");

    @Getter
    private final String permission;
}
