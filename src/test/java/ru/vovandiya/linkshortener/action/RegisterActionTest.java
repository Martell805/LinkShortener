package ru.vovandiya.linkshortener.action;

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
import ru.vovandiya.linkshortener.service.UserService;

@ExtendWith(MockitoExtension.class)
class RegisterActionTest {

  @Mock private UserService userService;

  @InjectMocks private RegisterAction registerAction;

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

      Field scannerField = RegisterAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(registerAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Register".equals(registerAction.getName());
  }

  @Test
  void run_ShouldRegisterUserSuccessfully() {
    // Given
    String userName = "Test User";
    setScannerInput(userName + "\n");
    when(userService.register(userName)).thenReturn(testUser);

    // When
    registerAction.run();

    // Then
    verify(userService).register(userName);
  }
}
