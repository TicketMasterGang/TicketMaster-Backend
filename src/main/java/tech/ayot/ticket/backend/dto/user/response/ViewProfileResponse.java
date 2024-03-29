package tech.ayot.ticket.backend.dto.user.response;

/**
 * Response body of view profile request.
 *
 * @see tech.ayot.ticket.backend.service.user.ProfileService ProfileService
 * @see tech.ayot.ticket.backend.model.user.User User
 */
public record ViewProfileResponse(
    Long version,
    String username,
    String firstName,
    String lastName,
    String profilePicture
) {
}
