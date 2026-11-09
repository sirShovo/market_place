package application.adapters.in.rest.responses;

public record UserResponse(
        Integer userId,
        String username,
        String identification,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String role,
        String status
) {
}
