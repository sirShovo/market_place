package application.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.DocumentId;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.UserRole;
import application.domain.valueobjects.UserStatus;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAdaptersTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bcryptPasswordAdapterEncryptsAndMatches() {
        BCryptPasswordAdapter adapter = new BCryptPasswordAdapter();

        String hash = adapter.encrypt("mySecretPassword");

        assertNotNull(hash);
        assertNotEquals("mySecretPassword", hash);
        assertTrue(adapter.matches("mySecretPassword", hash));
        assertFalse(adapter.matches("wrongPassword", hash));
        assertFalse(adapter.matches(null, hash));
    }

    @Test
    void jjwtAdapterGeneratesAndValidatesTokenClaims() {
        JjwtAdapter adapter = new JjwtAdapter(
                "MarketPlaceSuperSecretKeyForJWTSignature2024Minimum256BitsRequired!",
                86400000L);

        User user = new User();
        user.setUsername("ana");
        user.setRole(UserRole.SELLER);
        user.setIdentification(new DocumentId("DOC-ANA"));

        String token = adapter.generateToken(user);

        assertTrue(adapter.isTokenValid(token));
        assertEquals("ana", adapter.extractUsername(token));
        assertEquals("SELLER", adapter.extractRole(token));
        assertEquals("DOC-ANA", adapter.extractIdentification(token));
        assertFalse(adapter.isTokenValid(token + "tampered"));
        assertFalse(adapter.isTokenValid(""));
    }

    @Test
    void jwtAuthenticationFilterAndAuthenticatedUserResolverPopulateAndResolveUserAndBuyer() throws Exception {
        JjwtAdapter jjwtAdapter = new JjwtAdapter(
                "MarketPlaceSuperSecretKeyForJWTSignature2024Minimum256BitsRequired!",
                86400000L);
        UserRepositoryPort userRepo = mock(UserRepositoryPort.class);
        BuyerRepositoryPort buyerRepo = mock(BuyerRepositoryPort.class);

        User user = new User();
        user.setUserId(7);
        user.setUsername("carlos");
        user.setIdentification(new DocumentId("DOC-CARLOS"));
        user.setFullName("Carlos Buyer");
        user.setEmail(new Email("carlos@test.com"));
        user.setRole(UserRole.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepo.findByUsername(any(User.class))).thenReturn(Optional.of(user));

        Buyer buyer = new Buyer();
        buyer.setIdentification(new DocumentId("DOC-CARLOS"));
        buyer.setFullName("Carlos Buyer");
        buyer.setEmail(new Email("carlos@test.com"));
        buyer.setRole(UserRole.BUYER);
        buyer.setCommercialStatus(BuyerCommercialStatus.ACTIVE);
        buyer.setMainAddress("Cra 10");
        when(buyerRepo.findByIdentification(any(Buyer.class))).thenReturn(Optional.of(buyer));

        String token = jjwtAdapter.generateToken(user);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jjwtAdapter, userRepo);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        AuthenticatedUserResolver resolver = new AuthenticatedUserResolver(buyerRepo);
        User resolvedUser = resolver.resolveUser();
        Buyer resolvedBuyer = resolver.resolveBuyer();

        assertEquals("carlos", resolvedUser.getUsername());
        assertEquals(new DocumentId("DOC-CARLOS"), resolvedBuyer.getIdentification());
    }
}
