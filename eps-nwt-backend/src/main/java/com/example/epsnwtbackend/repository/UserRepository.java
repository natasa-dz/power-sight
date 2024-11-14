package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.activationToken = :token")
    User findByActivationToken(@Param("token") String token);

    @Query("SELECT u.userPhoto FROM User u WHERE u.id = :userId")
    String findUserPhotoPathById(@Param("userId") Long userId);
}
