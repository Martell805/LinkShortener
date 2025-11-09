package ru.vovandiya.linkshortener.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class LogoutAction implements AnonymousAction {
  private final UserService userService;

  @Override
  public String getName() {
    return "Logout";
  }

  @Override
  public void run() {
    userService.logout();
  }
}
