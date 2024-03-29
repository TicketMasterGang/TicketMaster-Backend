package tech.ayot.ticket.backend.service.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.ayot.ticket.backend.dto.user.request.UpdateProfileRequest;
import tech.ayot.ticket.backend.dto.user.response.ViewProfileResponse;
import tech.ayot.ticket.backend.model.user.User;
import tech.ayot.ticket.backend.repository.user.UserRepository;
import tech.ayot.ticket.backend.service.auth.AuthenticationService;

@RestController
@RequestMapping("/api/profile")
public class ProfileService {

    private final UserRepository userRepository;

    private final AuthenticationService authenticationService;

    private final PasswordEncoder passwordEncoder;

    public ProfileService(
        UserRepository userRepository,
        AuthenticationService authenticationService,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * @return View profile response of current logged-in user
     */
    @Transactional(readOnly = true)
    @GetMapping(value = {""}, produces = {"application/json"})
    public ResponseEntity<ViewProfileResponse> view() {
        User user = authenticationService.getCurrentUser();
        ViewProfileResponse viewProfileResponse = new ViewProfileResponse(
            user.getVersion(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getProfilePicture()
        );
        return new ResponseEntity<>(viewProfileResponse, HttpStatus.OK);
    }

    /**
     * Updates current logged-in user
     *
     * @param request The update request
     * @return New view profile response of current logged-in user
     */
    @Transactional
    @PostMapping(value = {"/update"}, consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<ViewProfileResponse> update(
        @RequestBody @Valid UpdateProfileRequest request
    ) {
        // Get user
        User user = authenticationService.getCurrentUser();

        if (!request.version().equals(user.getVersion())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Version is not correct");
        }

        // Update user fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.profilePicture() != null) {
            user.setProfilePicture(request.profilePicture());
        }

        // Update password
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();
        String newPasswordConfirmation = request.newPasswordConfirmation();
        if (newPassword != null) {
            if (!newPassword.equals(newPasswordConfirmation)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Password does not match");
            }
            if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Old password is wrong");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        // Save user
        userRepository.save(user);

        ViewProfileResponse viewProfileResponse = new ViewProfileResponse(
            user.getVersion(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getProfilePicture()
        );
        return new ResponseEntity<>(viewProfileResponse, HttpStatus.OK);
    }
}
