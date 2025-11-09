package ru.vovandiya.linkshortener.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
class ChangeMyLinkActionTest {

  @Mock private UserService userService;

  @Mock private LinkService linkService;

  @InjectMocks private ChangeMyLinkAction changeMyLinkAction;

  private User testUser;
  private Link testLink;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");

    testLink = new Link();
    testLink.setId(UUID.randomUUID());
    testLink.setShortLink("abc123");
    testLink.setDestination("https://example.com");
    testLink.setTimesUsed(0);
  }

  private void setScannerInput(String input) {
    try {
      InputStream inputStream = new ByteArrayInputStream(input.getBytes());
      Scanner scanner = new Scanner(inputStream);

      Field scannerField = ChangeMyLinkAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(changeMyLinkAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Change my link".equals(changeMyLinkAction.getName());
  }

  @Test
  void run_LinkNotFound_ShouldPrintErrorMessage() {
    // Given
    setScannerInput("nonexistent\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser(anyString(), any(User.class))).thenReturn(null);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService).getLinkForUser("nonexistent", testUser);
    verify(linkService, never()).save(any());
  }

  @Test
  void run_ChangeMaxUses_ShouldUpdateLink() {
    // Given
    setScannerInput("abc123\n1\n10\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService).save(testLink);
    assert testLink.getMaxUses() == 10;
  }

  @Test
  void run_ChangeMaxUsesToInfinite_ShouldSetNull() {
    // Given
    setScannerInput("abc123\n1\n\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService).save(testLink);
    assert testLink.getMaxUses() == null;
  }

  @Test
  void run_ChangeExpiration_ShouldUpdateLink() {
    // Given
    setScannerInput("abc123\n2\n24\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService).save(testLink);
    assert testLink.getExpiration() != null;
  }

  @Test
  void run_ChangeExpirationToInfinite_ShouldSetNull() {
    // Given
    setScannerInput("abc123\n2\n\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService).save(testLink);
    assert testLink.getExpiration() == null;
  }

  @Test
  void run_InvalidNumberFormat_ShouldPrintErrorMessage() {
    // Given
    setScannerInput("abc123\n1\ninvalid\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    changeMyLinkAction.run();

    // Then
    verify(linkService, never()).save(any());
  }
}
