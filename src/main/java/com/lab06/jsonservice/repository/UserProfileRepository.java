package com.lab06.jsonservice.repository;

import com.lab06.jsonservice.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ================================================================
 * UserProfileRepository - Profile DB CRUD interface
 * ================================================================
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * SOAP-ийн userId-аар профайл хайх
     * SQL: SELECT * FROM user_profiles WHERE user_id = ?
     * Frontend нэвтэрсний дараа userId-г ашиглаж профайл уншина
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * userId-аар профайл байгаа эсэхийг шалгах
     */
    boolean existsByUserId(Long userId);
}