package com.workpilot.entity.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {

    ADMIN(Set.of(
            Permission.ADMIN_READ,
            Permission.ADMIN_CREATE,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_DELETE,
            Permission.PROJECT_READ,
            Permission.PROJECT_CREATE,
            Permission.PROJECT_UPDATE,
            Permission.PROJECT_DELETE,
            Permission.RES_QUALITE_READ,
            Permission.RES_QUALITE_CREATE,
            Permission.RES_QUALITE_UPDATE,
            Permission.RES_QUALITE_DELETE,
            Permission.COORD_QUALITE_READ,
            Permission.COORD_QUALITE_UPDATE,
            Permission.ENGINEER_READ,
            Permission.ENGINEER_UPDATE
    )),
    RESPONSABLE_PROJET(Set.of(
            Permission.PROJECT_READ,
            Permission.PROJECT_CREATE,
            Permission.PROJECT_UPDATE,
            Permission.PROJECT_DELETE
    )),
    RESPONSABLE_QUALITE(Set.of(
            Permission.RES_QUALITE_READ,
            Permission.RES_QUALITE_CREATE,
            Permission.RES_QUALITE_UPDATE,
            Permission.RES_QUALITE_DELETE
    )),
    COORDINATEUR_QUALITE(Set.of(
            Permission.COORD_QUALITE_READ,
            Permission.COORD_QUALITE_UPDATE
    )),
    INGENIEUR(Set.of(
            Permission.ENGINEER_READ,
            Permission.ENGINEER_UPDATE
    ));

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
