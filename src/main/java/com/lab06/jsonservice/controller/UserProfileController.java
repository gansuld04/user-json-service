package com.lab06.jsonservice.controller;

import com.lab06.jsonservice.model.UserProfile;
import com.lab06.jsonservice.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ================================================================
 * UserProfileController - REST API Controller
 * ================================================================
 * Base URL: /users
 *
 * Endpoints:
 *   POST   /users           - Профайл үүсгэх
 *   GET    /users/{userId}  - Профайл унших
 *   PUT    /users/{userId}  - Профайл шинэчлэх
 *   DELETE /users/{userId}  - Профайл устгах
 *
 * Бүх endpoint-т AuthMiddleware ажиллаж token шалгана.
 * Token хүчинтэй бол request.getAttribute("userId") ашиглаж
 * хэн нэвтэрснийг мэдэж авч болно.
 * ================================================================
 */
@RestController
@RequestMapping("/users")
public class UserProfileController {

    @Autowired
    private UserProfileService profileService;

    // ================================================================
    // POST /users - Профайл үүсгэх
    // ================================================================

    /**
     * Шинэ профайл үүсгэх
     *
     * Request body (JSON):
     * {
     *   "userId": 1,
     *   "name": "Батбаяр",
     *   "email": "bat@example.com"
     * }
     *
     * Response (201 Created):
     * { "id": 1, "userId": 1, "name": "Батбаяр", ... }
     */
    @PostMapping
    public ResponseEntity<Object> createProfile(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        // AuthMiddleware-с тавьсан token-ийн userId авах
        Long tokenUserId = (Long) request.getAttribute("userId");

        // Request body-с утгуудыг авах
        Long userId = body.get("userId") != null
                ? Long.valueOf(body.get("userId").toString()) : tokenUserId;
        String name  = (String) body.get("name");
        String email = (String) body.get("email");

        UserProfileService.ServiceResult<UserProfile> result =
                profileService.createProfile(userId, name, email);

        if (!result.success) {
            return ResponseEntity.badRequest().body(errorResponse(result.message));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.data);
    }

    // ================================================================
    // GET /users/{userId} - Профайл унших
    // ================================================================

    /**
     * UserId-аар профайл авах
     *
     * Response (200 OK):
     * {
     *   "id": 1,
     *   "userId": 1,
     *   "name": "Батбаяр",
     *   "email": "bat@example.com",
     *   "bio": "Сайн уу!",
     *   "phone": "99001122",
     *   "location": "Улаанбаатар",
     *   "avatarUrl": null,
     *   "createdAt": "...",
     *   "updatedAt": "..."
     * }
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getProfile(@PathVariable Long userId) {

        UserProfileService.ServiceResult<UserProfile> result =
                profileService.getProfile(userId);

        if (!result.success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(result.message));
        }

        return ResponseEntity.ok(result.data);
    }

    // ================================================================
    // PUT /users/{userId} - Профайл шинэчлэх
    // ================================================================

    /**
     * Профайл шинэчлэх
     *
     * Request body (JSON) - зөвхөн өөрчлөх талбаруудыг илгээнэ:
     * {
     *   "bio": "Шинэ танилцуулга",
     *   "phone": "88001122",
     *   "location": "Дархан"
     * }
     *
     * Response (200 OK): шинэчлэгдсэн профайл
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfile updateData,
            HttpServletRequest request) {

        // Token-ийн userId болон URL-ийн userId таарч байгаа эсэхийг шалгах
        // (өөрийнхөө профайлыг л засаж болно)
        Long tokenUserId = (Long) request.getAttribute("userId");
        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("You can only update your own profile"));
        }

        UserProfileService.ServiceResult<UserProfile> result =
                profileService.updateProfile(userId, updateData);

        if (!result.success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(result.message));
        }

        return ResponseEntity.ok(result.data);
    }

    // ================================================================
    // DELETE /users/{userId} - Профайл устгах
    // ================================================================

    /**
     * Профайл устгах
     *
     * Response (200 OK):
     * { "message": "Profile deleted successfully" }
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteProfile(
            @PathVariable Long userId,
            HttpServletRequest request) {

        // Өөрийнхөө профайлыг л устгаж болно
        Long tokenUserId = (Long) request.getAttribute("userId");
        if (!userId.equals(tokenUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("You can only delete your own profile"));
        }

        UserProfileService.ServiceResult<Void> result = profileService.deleteProfile(userId);

        if (!result.success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(result.message));
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // Helper
    // ================================================================

    /**
     * Алдааны хариу JSON объект үүсгэх
     */
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}