package com.tagox.flow.security;

import com.tagox.flow.domain.role.RoleType;
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
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRoleRepository userRoleRepository
    ) {
        this.jwtService = jwtService;
        this.userRoleRepository = userRoleRepository;
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

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            userId,
                            tenantId
                    );

            List<RoleType> roleTypes =
                    userRoleRepository.findRoleTypesByUsuarioId(userId);

            var authorities = roleTypes.stream()
                    .map(roleType ->
                            new SimpleGrantedAuthority(
                                    "ROLE_" + roleType.name()
                            )
                    )
                    .toList();

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

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(request, response);
    }
}