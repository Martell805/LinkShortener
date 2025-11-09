package ru.vovandiya.linkshortener.service;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.vovandiya.linkshortener.entity.Link;
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.exception.LinkNotFoundException;
import ru.vovandiya.linkshortener.repository.LinkRepository;

@Service
@RequiredArgsConstructor
public class LinkService {
  private final LinkRepository linkRepository;

  @Value("${app.defaultLink}")
  private String defaultLink;

  public Link shortenLink(
      String destination, Integer maxUses, LocalDateTime expiration, User user) {
    var link =
        Link.builder()
            .destination(destination)
            .maxUses(maxUses)
            .timesUsed(0)
            .expiration(expiration)
            .generatedBy(user)
            .shortLink(defaultLink + "/" + getHash(user.getId().toString(), destination))
            .build();

    while (true) {
      try {
        link = linkRepository.save(link);
        break;
      } catch (DataIntegrityViolationException e) {
        link.setShortLink(defaultLink + "/" + getHash(user.getId().toString(), destination));
      }
    }

    return link;
  }

  public String getHash(String uuid, String destination) {
    try {
      String combined = uuid + "|" + destination + System.nanoTime();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(combined.getBytes());
      String hex = String.format("%064x", new BigInteger(1, digest));
      return hex.substring(0, 10);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Link> deleteAllExpiredLinks() {
    var links = linkRepository.getAllByExpirationBefore(LocalDateTime.now());
    linkRepository.deleteAll(links);
    return links;
  }

  public void useLink(String shortLink) throws URISyntaxException, IOException {
    var link = linkRepository.findByShortLink(shortLink).orElseThrow(LinkNotFoundException::new);

    if (link.getMaxUses() != null && link.getMaxUses() <= link.getTimesUsed()) {
      throw new LinkNotFoundException();
    }

    if (link.getExpiration() != null && link.getExpiration().isBefore(LocalDateTime.now())) {
      throw new LinkNotFoundException();
    }

    link.setTimesUsed(link.getTimesUsed() + 1);
    linkRepository.save(link);

    if (link.getMaxUses() != null && link.getTimesUsed() >= link.getMaxUses()) {
      System.out.printf("[INFO] Link %s reached maximum number of uses\n", link.getShortLink());
    }

    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      Desktop.getDesktop().browse(new URI(link.getDestination()));
    } else {
      System.out.printf("Link: %s\n", link.getDestination());
      System.out.println("Cannot open browser automatically. Please open the link manually.");
    }
  }

  public List<Link> getAllForUser(User user) {
    return linkRepository.getAllByGeneratedBy(user);
  }

  public Link getLinkForUser(String shortLink, User user) {
    return linkRepository.getByShortLinkAndGeneratedBy(shortLink, user);
  }

  public void save(Link link) {
    linkRepository.save(link);
  }

  public void delete(Link link) {
    linkRepository.delete(link);
  }
}
