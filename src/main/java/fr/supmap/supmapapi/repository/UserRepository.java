package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByOauth2Id(Integer oauth2Id);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByOauth2Id(BigInteger oauth2Id);
}