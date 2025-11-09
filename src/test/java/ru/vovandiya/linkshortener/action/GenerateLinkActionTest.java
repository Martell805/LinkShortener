package ru.vovandiya.linkshortener.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
class GenerateLinkActionTest {

  @Mock private LinkService linkService;

  @Mock private UserService userService;

  @InjectMocks private GenerateLinkAction generateLinkAction;

  private User testUser;
  private Link testLink;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");

    testLink = new Link();
    testLink.setId(UUID.randomUUID());
    testLink.setShortLink("short123");
  }

  private void setScannerInput(String input) {
    try {
      InputStream inputStream = new ByteArrayInputStream(input.getBytes());
      Scanner scanner = new Scanner(inputStream);

      Field scannerField = GenerateLinkAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(generateLinkAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Generate link".equals(generateLinkAction.getName());
  }

  @Test
  void run_ValidInput_ShouldGenerateLink() {
    // Given
    setScannerInput("https://example.com\n5\n24\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.shortenLink(anyString(), any(), any(), any())).thenReturn(testLink);

    // When
    generateLinkAction.run();

    // Then
    verify(linkService)
        .shortenLink(eq("https://example.com"), eq(5), any(LocalDateTime.class), eq(testUser));
  }

  @Test
  void run_InfiniteUsesAndExpiration_ShouldGenerateLinkWithNullValues() {
    // Given
    setScannerInput("https://example.com\n\n\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.shortenLink(anyString(), isNull(), isNull(), any())).thenReturn(testLink);

    // When
    generateLinkAction.run();

    // Then
    verify(linkService).shortenLink("https://example.com", null, null, testUser);
  }

  @Test
  void run_InvalidNumberFormat_ShouldPrintErrorMessage() {
    // Given
    setScannerInput("https://example.com\ninvalid\n24\n");

    // When
    generateLinkAction.run();

    // Then
    verify(linkService, never()).shortenLink(anyString(), any(), any(), any());
  }
}
