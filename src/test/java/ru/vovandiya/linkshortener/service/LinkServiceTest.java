package ru.vovandiya.linkshortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.vovandiya.linkshortener.entity.Link;
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.exception.LinkNotFoundException;
import ru.vovandiya.linkshortener.repository.LinkRepository;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

  @Mock private LinkRepository linkRepository;

  @InjectMocks private LinkService linkService;

  private User testUser;
  private Link testLink;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");

    testLink = new Link();
    testLink.setId(UUID.randomUUID());
    testLink.setShortLink("http://localhost/abc123");
    testLink.setDestination("https://example.com");
    testLink.setTimesUsed(0);
    testLink.setMaxUses(10);
    testLink.setExpiration(LocalDateTime.now().plusHours(24));
    testLink.setGeneratedBy(testUser);
  }

  @Test
  void shortenLink_ValidInput_ShouldReturnLink() {
    // Given
    String destination = "https://example.com";
    Integer maxUses = 5;
    LocalDateTime expiration = LocalDateTime.now().plusHours(24);

    when(linkRepository.save(any(Link.class))).thenReturn(testLink);

    // When
    Link result = linkService.shortenLink(destination, maxUses, expiration, testUser);

    // Then
    assertNotNull(result);
    verify(linkRepository).save(any(Link.class));
  }

  @Test
  void shortenLink_WithHashCollision_ShouldRetryWithNewHash() {
    // Given
    String destination = "https://example.com";

    when(linkRepository.save(any(Link.class)))
        .thenThrow(DataIntegrityViolationException.class)
        .thenReturn(testLink);

    // When
    Link result = linkService.shortenLink(destination, null, null, testUser);

    // Then
    assertNotNull(result);
    verify(linkRepository, times(2)).save(any(Link.class));
  }

  @Test
  void getHash_ShouldReturnValidHash() {
    // Given
    String uuid = UUID.randomUUID().toString();
    String destination = "https://example.com";

    // When
    String hash = linkService.getHash(uuid, destination);

    // Then
    assertNotNull(hash);
    assertEquals(10, hash.length());
    assertTrue(hash.matches("[a-f0-9]+"));
  }

  @Test
  void deleteAllExpiredLinks_ShouldDeleteExpiredLinks() {
    // Given
    List<Link> expiredLinks = List.of(testLink);
    when(linkRepository.getAllByExpirationBefore(any(LocalDateTime.class)))
        .thenReturn(expiredLinks);

    // When
    List<Link> result = linkService.deleteAllExpiredLinks();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(linkRepository).deleteAll(expiredLinks);
  }

  @Test
  void useLink_ValidLink_ShouldIncrementTimesUsed() throws Exception {
    // Given
    String shortLink = "http://localhost/abc123";
    when(linkRepository.findByShortLink(shortLink)).thenReturn(Optional.of(testLink));
    when(linkRepository.save(any(Link.class))).thenReturn(testLink);

    // We'll test the logic up to the Desktop call
    // The actual browser opening is not critical for unit tests

    // When
    linkService.useLink(shortLink);

    // Then
    verify(linkRepository).save(testLink);
    assertEquals(1, testLink.getTimesUsed());
  }

  @Test
  void useLink_LinkNotFound_ShouldThrowException() {
    // Given
    String shortLink = "nonexistent";
    when(linkRepository.findByShortLink(shortLink)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(LinkNotFoundException.class, () -> linkService.useLink(shortLink));
  }

  @Test
  void useLink_MaxUsesExceeded_ShouldThrowException() {
    // Given
    testLink.setTimesUsed(10);
    testLink.setMaxUses(10);
    when(linkRepository.findByShortLink(testLink.getShortLink())).thenReturn(Optional.of(testLink));

    // When & Then
    assertThrows(LinkNotFoundException.class, () -> linkService.useLink(testLink.getShortLink()));
  }

  @Test
  void useLink_ExpiredLink_ShouldThrowException() {
    // Given
    testLink.setExpiration(LocalDateTime.now().minusHours(1));
    when(linkRepository.findByShortLink(testLink.getShortLink())).thenReturn(Optional.of(testLink));

    // When & Then
    assertThrows(LinkNotFoundException.class, () -> linkService.useLink(testLink.getShortLink()));
  }

  @Test
  void useLink_InfiniteUses_ShouldWorkCorrectly() throws Exception {
    // Given
    testLink.setMaxUses(null); // Infinite uses
    when(linkRepository.findByShortLink(testLink.getShortLink())).thenReturn(Optional.of(testLink));
    when(linkRepository.save(any(Link.class))).thenReturn(testLink);

    // When
    linkService.useLink(testLink.getShortLink());

    // Then
    verify(linkRepository).save(testLink);
    assertEquals(1, testLink.getTimesUsed());
  }

  @Test
  void useLink_NoExpiration_ShouldWorkCorrectly() throws Exception {
    // Given
    testLink.setExpiration(null); // No expiration
    when(linkRepository.findByShortLink(testLink.getShortLink())).thenReturn(Optional.of(testLink));
    when(linkRepository.save(any(Link.class))).thenReturn(testLink);

    // When
    linkService.useLink(testLink.getShortLink());

    // Then
    verify(linkRepository).save(testLink);
    assertEquals(1, testLink.getTimesUsed());
  }

  @Test
  void getAllForUser_ShouldReturnUserLinks() {
    // Given
    List<Link> userLinks = List.of(testLink);
    when(linkRepository.getAllByGeneratedBy(testUser)).thenReturn(userLinks);

    // When
    List<Link> result = linkService.getAllForUser(testUser);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(linkRepository).getAllByGeneratedBy(testUser);
  }

  @Test
  void getLinkForUser_ShouldReturnUserLink() {
    // Given
    when(linkRepository.getByShortLinkAndGeneratedBy(testLink.getShortLink(), testUser))
        .thenReturn(testLink);

    // When
    Link result = linkService.getLinkForUser(testLink.getShortLink(), testUser);

    // Then
    assertNotNull(result);
    assertEquals(testLink, result);
    verify(linkRepository).getByShortLinkAndGeneratedBy(testLink.getShortLink(), testUser);
  }

  @Test
  void save_ShouldCallRepositorySave() {
    // When
    linkService.save(testLink);

    // Then
    verify(linkRepository).save(testLink);
  }

  @Test
  void delete_ShouldCallRepositoryDelete() {
    // When
    linkService.delete(testLink);

    // Then
    verify(linkRepository).delete(testLink);
  }
}
