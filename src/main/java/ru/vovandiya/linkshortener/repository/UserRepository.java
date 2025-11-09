package ru.vovandiya.linkshortener.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vovandiya.linkshortener.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {}
