package ru.vovandiya.linkshortener.action;

import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class RegisterAction implements AnonymousAction {
  private final UserService userService;
  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Register";
  }

  @Override
  public void run() {
    System.out.println("Enter your name:");
    var name = scanner.nextLine();
    var user = userService.register(name);
    System.out.printf("Hello %s! Your uuid is %s\n", user.getName(), user.getId());
  }
}
