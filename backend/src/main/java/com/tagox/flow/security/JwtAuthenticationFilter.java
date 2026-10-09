package com.tagox.flow.security;

import com.tagox.flow.domain.role.RoleType;
import com.tagox.flow.domain.tenant.Tenant;
import com.tagox.flow.domain.tenant.TenantRepository;
import com.tagox.flow.domain.tenant.TenantStatus;
import com.tagox.flow.domain.user.User;
import com.tagox.flow.domain.user.UserRepository;
import com.tagox.flow.domain.user.UserStatus;
import com.tagox.flow.domain.userrole.UserRoleRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository
    ) {
        this.jwtService = jwtService;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            UUID userId = jwtService.extrairUserId(token);
            UUID tenantId = jwtService.extrairTenantId(token);

            Optional<User> userOptional =
                    userRepository.findByIdAndTenantId(
                            userId,
                            tenantId
                    );

            if (userOptional.isEmpty()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            User user = userOptional.get();

            if (user.getStatus() != UserStatus.ATIVO) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Optional<Tenant> tenantOptional =
                    tenantRepository.findById(tenantId);

            if (tenantOptional.isEmpty()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Tenant tenant = tenantOptional.get();
            TenantStatus tenantStatus = tenant.getStatus();

            if (tenantStatus == TenantStatus.SUSPENSO ||
                    tenantStatus == TenantStatus.CANCELADO) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            List<RoleType> roleTypes =
                    userRoleRepository.findRoleTypesByUsuarioId(userId);

            var authorities = roleTypes.stream()
                    .map(roleType ->
                            new SimpleGrantedAuthority(
                                    "ROLE_" + roleType.name()
                            )
                    )
                    .toList();

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            userId,
                            tenantId
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}