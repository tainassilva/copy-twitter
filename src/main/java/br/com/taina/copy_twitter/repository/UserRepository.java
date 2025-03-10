package br.com.taina.copy_twitter.repository;

import br.com.taina.copy_twitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Verifica se o username existe ...
    // SELECT * FROM user WHERE username = ?;
    Optional<User> findByUsername(String username);
}
