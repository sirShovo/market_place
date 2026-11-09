package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Stateless JWT filter that resolves the authenticated domain {@link User} and places it
 * inside the Spring {@link SecurityContextHolder}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JjwtAdapter jjwtAdapter;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (jjwtAdapter.isTokenValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jjwtAdapter.extractUsername(token);
            User probe = new User();
            probe.setUsername(username);

            User authenticatedUser = userRepositoryPort.findByUsername(probe)
                    .orElseGet(() -> buildFallbackUserFromToken(token, username));

            List<SimpleGrantedAuthority> authorities = authenticatedUser.getRole() != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.getRole().getCode()))
                    : List.of();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private User buildFallbackUserFromToken(String token, String username) {
        User user = new User();
        user.setUsername(username);
        user.setStatus(UserStatus.ACTIVE);
        String roleCode = jjwtAdapter.extractRole(token);
        if (roleCode != null) {
            user.setRole(UserRole.fromCode(roleCode));
        }
        String identification = jjwtAdapter.extractIdentification(token);
        if (identification != null && !identification.isBlank()) {
            user.setIdentification(new DocumentId(identification));
        }
        return user;
    }
}
