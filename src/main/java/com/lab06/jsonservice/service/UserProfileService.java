package com.lab06.jsonservice.service;

import com.lab06.jsonservice.model.UserProfile;
import com.lab06.jsonservice.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ================================================================
 * UserProfileService - Profile CRUD business logic
 * ================================================================
 * REST API-ийн бүх логик энд байна:
 *   - createProfile  : POST /users
 *   - getProfile     : GET  /users/:id
 *   - updateProfile  : PUT  /users/:id
 *   - deleteProfile  : DELETE /users/:id
 *
 * NOTE: :id нь UserProfile.id биш, SOAP-ийн userId!
 *       userId-аар хайж профайл олно.
 * ================================================================
 */
@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository profileRepository;

    // ================================================================
    // CREATE - POST /users
    // ================================================================

    /**
     * Шинэ профайл үүсгэх
     * Register хийсний дараа frontend энийг дуудна
     *
     * @param userId  SOAP service-с авсан userId
     * @param name    хэрэглэгчийн нэр
     * @param email   и-мэйл
     * @return үүсгэсэн профайл
     */
    public ServiceResult<UserProfile> createProfile(Long userId, String name, String email) {

        // userId давхардаж байгаа эсэхийг шалгах
        if (profileRepository.existsByUserId(userId)) {
            return ServiceResult.error("Profile already exists for this user");
        }

        if (name == null || name.trim().isEmpty()) {
            return ServiceResult.error("Name is required");
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setName(name.trim());
        profile.setEmail(email);

        UserProfile saved = profileRepository.save(profile);
        return ServiceResult.success(saved);
    }

    // ================================================================
    // READ - GET /users/:userId
    // ================================================================

    /**
     * UserId-аар профайл унших
     *
     * @param userId SOAP-ийн userId
     * @return профайл эсвэл алдаа
     */
    public ServiceResult<UserProfile> getProfile(Long userId) {
        Optional<UserProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            return ServiceResult.error("Profile not found");
        }

        return ServiceResult.success(profileOpt.get());
    }

    // ================================================================
    // UPDATE - PUT /users/:userId
    // ================================================================

    /**
     * Профайл шинэчлэх
     * Frontend-с ирсэн утгуудыг existing профайл дээр хэрэглэнэ
     *
     * @param userId     SOAP-ийн userId
     * @param updateData шинэчлэх мэдээлэл агуулсан UserProfile
     * @return шинэчлэгдсэн профайл
     */
    public ServiceResult<UserProfile> updateProfile(Long userId, UserProfile updateData) {
        Optional<UserProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            return ServiceResult.error("Profile not found");
        }

        UserProfile profile = profileOpt.get();

        // Null биш утгуудыг л шинэчлэнэ (partial update)
        if (updateData.getName() != null && !updateData.getName().trim().isEmpty()) {
            profile.setName(updateData.getName().trim());
        }
        if (updateData.getBio() != null) {
            profile.setBio(updateData.getBio());
        }
        if (updateData.getPhone() != null) {
            profile.setPhone(updateData.getPhone());
        }
        if (updateData.getLocation() != null) {
            profile.setLocation(updateData.getLocation());
        }
        if (updateData.getAvatarUrl() != null) {
            profile.setAvatarUrl(updateData.getAvatarUrl());
        }

        UserProfile updated = profileRepository.save(profile);
        return ServiceResult.success(updated);
    }

    // ================================================================
    // DELETE - DELETE /users/:userId
    // ================================================================

    /**
     * Профайл устгах
     *
     * @param userId SOAP-ийн userId
     * @return амжилт эсэх
     */
    public ServiceResult<Void> deleteProfile(Long userId) {
        Optional<UserProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            return ServiceResult.error("Profile not found");
        }

        profileRepository.delete(profileOpt.get());
        return ServiceResult.success(null);
    }

    // ================================================================
    // ServiceResult - Controller-д буцаах wrapper класс
    // ================================================================

    public static class ServiceResult<T> {
        public final boolean success;
        public final String message;
        public final T data;

        private ServiceResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ServiceResult<T> success(T data) {
            return new ServiceResult<>(true, null, data);
        }

        public static <T> ServiceResult<T> error(String message) {
            return new ServiceResult<>(false, message, null);
        }
    }
}