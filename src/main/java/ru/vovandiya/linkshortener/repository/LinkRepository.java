package ru.vovandiya.linkshortener.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vovandiya.linkshortener.entity.Link;
import ru.vovandiya.linkshortener.entity.User;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {
  List<Link> getAllByExpirationBefore(LocalDateTime now);

  Optional<Link> findByShortLink(String shortLink);

  List<Link> getAllByGeneratedBy(User user);

  Link getByShortLinkAndGeneratedBy(String shortLink, User user);
}
