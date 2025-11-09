package ru.vovandiya.linkshortener.action;

import static org.mockito.ArgumentMatchers.any;
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
class DeleteMyLinkActionTest {

  @Mock private UserService userService;

  @Mock private LinkService linkService;

  @InjectMocks private DeleteMyLinkAction deleteMyLinkAction;

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
  }

  private void setScannerInput(String input) {
    try {
      InputStream inputStream = new ByteArrayInputStream(input.getBytes());
      Scanner scanner = new Scanner(inputStream);

      Field scannerField = DeleteMyLinkAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(deleteMyLinkAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Delete my link".equals(deleteMyLinkAction.getName());
  }

  @Test
  void run_LinkFound_ShouldDeleteLink() {
    // Given
    setScannerInput("abc123\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("abc123", testUser)).thenReturn(testLink);

    // When
    deleteMyLinkAction.run();

    // Then
    verify(linkService).delete(testLink);
  }

  @Test
  void run_LinkNotFound_ShouldPrintErrorMessage() {
    // Given
    setScannerInput("nonexistent\n");
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(linkService.getLinkForUser("nonexistent", testUser)).thenReturn(null);

    // When
    deleteMyLinkAction.run();

    // Then
    verify(linkService, never()).delete(any());
  }
}
