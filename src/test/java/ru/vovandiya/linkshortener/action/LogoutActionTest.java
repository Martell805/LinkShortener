package ru.vovandiya.linkshortener.action;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vovandiya.linkshortener.service.UserService;

@ExtendWith(MockitoExtension.class)
class LogoutActionTest {

  @Mock private UserService userService;

  @InjectMocks private LogoutAction logoutAction;

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Logout".equals(logoutAction.getName());
  }

  @Test
  void run_ShouldCallUserServiceLogout() {
    // When
    logoutAction.run();

    // Then
    verify(userService).logout();
  }
}
