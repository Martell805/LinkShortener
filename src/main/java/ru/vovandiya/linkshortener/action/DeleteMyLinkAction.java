package ru.vovandiya.linkshortener.action;

import jakarta.transaction.Transactional;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.LinkService;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class DeleteMyLinkAction implements Action {
  private final UserService userService;
  private final LinkService linkService;

  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Delete my link";
  }

  @Transactional
  @Override
  public void run() {
    System.out.println("Enter link:");
    var shortLink = scanner.nextLine();

    var link = linkService.getLinkForUser(shortLink, userService.getCurrentUser());
    if (link == null) {
      System.out.println("Link not found!");
      return;
    }
    linkService.delete(link);

    System.out.println("Link was successfully deleted!");
  }
}
