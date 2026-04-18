package com.hitster.controller;

import com.hitster.dto.LeaderboardDTO;
import com.hitster.dto.UpdateMeRequestDTO;
import com.hitster.dto.UserMeDTO;
import com.hitster.service.DatabaseService;
import com.hitster.service.EmailValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public UserMeDTO getMe(HttpServletRequest request) {
        Long userId = getJwtUserId(request);

        UserMeDTO user = DatabaseService.getUserMeById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        return user;
    }

    @GetMapping("/leaderboard")
    public LeaderboardDTO getLeaderboard() {
        return DatabaseService.getLeaderboard();
    }

    @PutMapping("/me")
    public UserMeDTO updateMe(@RequestBody UpdateMeRequestDTO request,
                              HttpServletRequest httpRequest) {
        Long userId = getJwtUserId(httpRequest);

        if (request == null ||
                isBlank(request.getUsername()) ||
                isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Username and email are required.");
        }

        if (!EmailValidationUtil.isValidEmail(request.getEmail())) {
            throw new InvalidEmailException("Invalid email format or domain.");
        }

        validateProfileImage(request.getProfilePicturePath());

        if (DatabaseService.usernameExistsForOtherUser(userId, request.getUsername())) {
            throw new DuplicateFieldException("TAKEN_USERNAME_ERR", "Username is already taken.");
        }

        if (DatabaseService.emailExistsForOtherUser(userId, request.getEmail())) {
            throw new DuplicateFieldException("TAKEN_EMAIL_ERR", "Email is already taken.");
        }

        boolean updated = DatabaseService.updateCurrentUser(userId, request);
        if (!updated) {
            throw new IllegalArgumentException("User update failed.");
        }

        UserMeDTO user = DatabaseService.getUserMeById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        return user;
    }

    @DeleteMapping("/me")
    public String deleteMe(HttpServletRequest request) {
        Long userId = getJwtUserId(request);

        boolean deleted = DatabaseService.deleteUserById(userId);
        if (!deleted) {
            throw new NotFoundException("User not found: " + userId);
        }

        return "OK";
    }

    private Long getJwtUserId(HttpServletRequest request) {
        Object jwtUserIdObj = request.getAttribute("jwtUserId");

        if (jwtUserIdObj == null) {
            throw new IllegalArgumentException("Missing authenticated user.");
        }

        if (jwtUserIdObj instanceof Integer) {
            return ((Integer) jwtUserIdObj).longValue();
        }

        if (jwtUserIdObj instanceof Long) {
            return (Long) jwtUserIdObj;
        }

        return Long.parseLong(String.valueOf(jwtUserIdObj));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateProfileImage(String profilePicturePath) {
        if (profilePicturePath == null || profilePicturePath.trim().isEmpty()) {
            return;
        }

        String value = profilePicturePath.trim();

        if (!value.startsWith("data:image/")) {
            throw new InvalidImageFormatException("Profile picture must be a Base64 image data URL.");
        }

        int commaIndex = value.indexOf(',');
        if (commaIndex <= 0) {
            throw new InvalidImageFormatException("Profile picture is not a valid Base64 image.");
        }

        String metadata = value.substring(0, commaIndex).toLowerCase();
        String base64Part = value.substring(commaIndex + 1).trim();

        if (!metadata.contains(";base64")) {
            throw new InvalidImageFormatException("Profile picture must be Base64-encoded.");
        }

        if (base64Part.isEmpty()) {
            throw new InvalidImageFormatException("Profile picture Base64 content is empty.");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Part);

            if (decoded.length == 0) {
                throw new InvalidImageFormatException("Profile picture is empty.");
            }

            if (decoded.length > 5 * 1024 * 1024) {
                throw new InvalidImageFormatException("Profile picture exceeds size limit.");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidImageFormatException("Profile picture is not a valid Base64 image.");
        }
    }
}