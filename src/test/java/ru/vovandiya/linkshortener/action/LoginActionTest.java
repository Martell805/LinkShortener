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
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.exception.UserNotFoundException;
import ru.vovandiya.linkshortener.service.UserService;

@ExtendWith(MockitoExtension.class)
class LoginActionTest {

  @Mock private UserService userService;

  @InjectMocks private LoginAction loginAction;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");
  }

  private void setScannerInput(String input) {
    try {
      InputStream inputStream = new ByteArrayInputStream(input.getBytes());
      Scanner scanner = new Scanner(inputStream);

      Field scannerField = LoginAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(loginAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Login".equals(loginAction.getName());
  }

  @Test
  void run_ValidUUID_ShouldLoginSuccessfully() {
    // Given
    UUID testUuid = UUID.randomUUID();
    setScannerInput(testUuid.toString() + "\n");
    when(userService.login(testUuid)).thenReturn(testUser);

    // When
    loginAction.run();

    // Then
    verify(userService).login(testUuid);
  }

  @Test
  void run_InvalidUUIDFormat_ShouldPrintErrorMessage() {
    // Given
    setScannerInput("invalid-uuid\n");

    // When
    loginAction.run();

    // Then
    verify(userService, never()).login(any());
  }

  @Test
  void run_UserNotFound_ShouldPrintErrorMessage() {
    // Given
    UUID testUuid = UUID.randomUUID();
    setScannerInput(testUuid + "\n");
    when(userService.login(testUuid)).thenThrow(new UserNotFoundException());

    // When
    loginAction.run();

    // Then
    verify(userService).login(testUuid);
  }
}
