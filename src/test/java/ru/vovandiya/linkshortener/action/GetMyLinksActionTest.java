package ru.vovandiya.linkshortener.action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vovandiya.linkshortener.entity.Link;
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.service.LinkService;
import ru.vovandiya.linkshortener.service.UserService;

@ExtendWith(MockitoExtension.class)
class GetMyLinksActionTest {

  @Mock private UserService userService;

  @Mock private LinkService linkService;

  @InjectMocks private GetMyLinksAction getMyLinksAction;

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Get my links".equals(getMyLinksAction.getName());
  }

  @Test
  void run_ShouldPrintUserLinks() {
    // Given
    User testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");

    Link link1 = new Link();
    link1.setId(UUID.randomUUID());
    link1.setShortLink("abc123");
    link1.setDestination("https://example1.com");
    link1.setTimesUsed(5);
    link1.setMaxUses(10);
    link1.setExpiration(LocalDateTime.now().plusHours(24));

    Link link2 = new Link();
    link2.setId(UUID.randomUUID());
    link2.setShortLink("def456");
    link2.setDestination("https://example2.com");
    link2.setTimesUsed(2);
    link2.setMaxUses(null);
    link2.setExpiration(null);

    List<Link> links = Arrays.asList(link1, link2);

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getAllForUser(testUser)).thenReturn(links);

    // When
    getMyLinksAction.run();

    // Then
    verify(linkService).getAllForUser(testUser);
  }

  @Test
  void run_NoLinks_ShouldNotFail() {
    // Given
    User testUser = new User();
    testUser.setId(UUID.randomUUID());

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getAllForUser(testUser)).thenReturn(List.of());

    // When
    getMyLinksAction.run();

    // Then
    verify(linkService).getAllForUser(testUser);
  }
}
