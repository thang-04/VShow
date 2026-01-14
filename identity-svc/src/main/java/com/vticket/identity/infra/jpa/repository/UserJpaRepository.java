package com.vticket.identity.infra.jpa.repository;

import com.vticket.identity.app.dto.req.UpdateProfileRequest;
import com.vticket.identity.infra.jpa.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByDeviceId(String deviceId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Modifying
    @Transactional
    @Query("""
                UPDATE UserEntity u
                SET 
                    u.avatar = :avatar,
                    u.email = :email,
                    u.address = :address,
                    u.phone= :phone,
                    u.updatedAt = CURRENT_TIMESTAMP
                WHERE u.id = :uid
            """)
    int updateProfile(
            @Param("uid") String uid,
            @Param("avatar") String avatar,
            @Param("email") String email,
            @Param("address") String address,
            @Param("phone") String phone
    );
}

