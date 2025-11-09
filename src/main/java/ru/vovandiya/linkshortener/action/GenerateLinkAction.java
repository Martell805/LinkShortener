package ru.vovandiya.linkshortener.action;

import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.LinkService;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class GenerateLinkAction implements Action {
  private final LinkService linkService;
  private final UserService userService;

  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Generate link";
  }

  @Transactional
  @Override
  public void run() {
    System.out.println("Enter link:");
    var link = scanner.nextLine();
    System.out.println("Enter max uses (blank for infinite):");
    var maxUsesInput = scanner.nextLine();
    System.out.println("Enter expiration date in hours (blank for infinite):");
    var expirationHoursInput = scanner.nextLine();

    try {
      new URI(link);

      Integer maxUses;
      LocalDateTime expiration;

      if (maxUsesInput.isBlank()) {
        maxUses = null;
      } else {
        maxUses = Integer.parseInt(maxUsesInput);
      }

      if (expirationHoursInput.isBlank()) {
        expiration = null;
      } else {
        expiration = LocalDateTime.now().plusHours(Long.parseLong(expirationHoursInput));
      }

      var newLink =
          linkService.shortenLink(link, maxUses, expiration, userService.getCurrentUser());

      System.out.printf("Generated link: %s\n", newLink.getShortLink());
    } catch (NumberFormatException e) {
      System.out.println("Incorrect number format");
    } catch (URISyntaxException e) {
      System.out.println("Incorrect link format");
    }
  }
}
