package ru.vovandiya.linkshortener.cli;

import java.util.List;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.action.Action;
import ru.vovandiya.linkshortener.action.AnonymousAction;
import ru.vovandiya.linkshortener.exception.ExitException;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class UserInputCLI implements CommandLineRunner {
  private final UserService userService;

  private final List<Action> allActions;
  private final List<AnonymousAction> anonymousActions;

  private final Scanner scanner = new Scanner(System.in);

  @Override
  public void run(String... args) {
    System.setProperty("java.awt.headless", "false");

    while (true) {
      try {
        handleUserInput();
      } catch (ExitException e) {
        System.exit(0);
      }
    }
  }

  private void handleUserInput() {
    if (userService.getCurrentUser() == null) {
      System.out.println("Anonymous user");
      handleActions(anonymousActions);
    } else {
      System.out.printf("Current user: %s\n", userService.getCurrentUser().getName());
      handleActions(allActions);
    }
  }

  private void handleActions(List<? extends Action> actions) {
    System.out.println("Available actions:");
    for (int i = 1; i <= actions.size(); i++) {
      System.out.printf("\t%d. %s\n", i, actions.get(i - 1).getName());
    }

    System.out.println("Choose action:");
    var input = scanner.nextLine();

    try {
      var actionNumber = Integer.parseInt(input);
      var action = actions.get(actionNumber - 1);
      action.run();
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      System.out.printf("'%s' is incorrect choice!\n", input);
    }
  }
}
