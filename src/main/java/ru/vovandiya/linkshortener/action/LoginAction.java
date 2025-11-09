package ru.vovandiya.linkshortener.action;

import java.util.Scanner;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.exception.UserNotFoundException;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class LoginAction implements AnonymousAction {
  private final UserService userService;
  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Login";
  }

  @Override
  public void run() {
    System.out.println("Enter your UUID:");
    var uuid = scanner.nextLine();
    try {
      var user = userService.login(UUID.fromString(uuid));
      System.out.printf("Hello %s!\n", user.getName());
    } catch (IllegalArgumentException e) {
      System.out.printf("UUID %s have incorrect format\n", uuid);
    } catch (UserNotFoundException e) {
      System.out.printf("User with uuid %s not found\n", uuid);
    }
  }
}
