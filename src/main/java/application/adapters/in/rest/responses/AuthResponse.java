package application.adapters.in.rest.responses;

public record AuthResponse(
        String token,
        String username
) {
}
