package com.healthcare.hms.security.principal;

import com.healthcare.hms.security.SecurityConstants;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal populated from validated JWT claims.
 * Designed for future RBAC: roles and permissions are exposed as authorities.
 */
public final class AuthenticatedUser implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String email;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final long tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(
            final UUID userId,
            final UUID tenantId,
            final String email,
            final Set<String> roles,
            final Set<String> permissions,
            final long tokenVersion
    ) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
        this.tokenVersion = tokenVersion;
        this.authorities = buildAuthorities(this.roles, this.permissions);
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(
            final Set<String> roles,
            final Set<String> permissions
    ) {
        final java.util.HashSet<GrantedAuthority> authorities = new java.util.HashSet<>();

        roles.stream()
                .map(role -> role.startsWith(SecurityConstants.ROLE_PREFIX)
                        ? role
                        : SecurityConstants.ROLE_PREFIX + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return Set.copyOf(authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public long getTokenVersion() {
        return tokenVersion;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
