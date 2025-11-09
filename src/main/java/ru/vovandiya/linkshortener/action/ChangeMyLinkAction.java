package ru.vovandiya.linkshortener.action;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.LinkService;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class ChangeMyLinkAction implements Action {
  private final UserService userService;
  private final LinkService linkService;

  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Change my link";
  }

  @Transactional
  @Override
  public void run() {
    System.out.println("Enter link:");
    var shortLink = scanner.nextLine();

    try {
      var link = linkService.getLinkForUser(shortLink, userService.getCurrentUser());
      if (link == null) {
        System.out.println("Link not found!");
        return;
      }

      System.out.println("What parameter do you want to change?");
      System.out.println("\t1. Max uses");
      System.out.println("\t2. Expiration date");

      var choice = scanner.nextLine();

      switch (choice) {
        case "1":
          System.out.println("Enter max uses (blank for infinite):");
          var maxUsesInput = scanner.nextLine();
          Integer maxUses;

          if (maxUsesInput.isBlank()) {
            maxUses = null;
          } else {
            maxUses = Integer.parseInt(maxUsesInput);
          }
          link.setMaxUses(maxUses);
          linkService.save(link);
          break;
        case "2":
          System.out.println("Enter expiration date in hours (blank for infinite):");
          var expirationHoursInput = scanner.nextLine();

          LocalDateTime expiration;
          if (expirationHoursInput.isBlank()) {
            expiration = null;
          } else {
            expiration = LocalDateTime.now().plusHours(Long.parseLong(expirationHoursInput));
          }
          link.setExpiration(expiration);
          linkService.save(link);
          break;
      }
      System.out.println("Link was successfully changed!");
    } catch (NumberFormatException e) {
      System.out.println("Incorrect number format");
    }
  }
}
