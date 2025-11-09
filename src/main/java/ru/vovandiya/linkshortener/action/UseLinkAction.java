package ru.vovandiya.linkshortener.action;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.exception.LinkNotFoundException;
import ru.vovandiya.linkshortener.service.LinkService;

@Component
@RequiredArgsConstructor
public class UseLinkAction implements AnonymousAction {
  private final LinkService linkService;
  private final Scanner scanner = new Scanner(System.in);

  @Override
  public String getName() {
    return "Use link";
  }

  @Override
  public void run() {
    System.out.println("Enter link:");
    var link = scanner.nextLine();
    try {
      linkService.useLink(link);
    } catch (URISyntaxException | IOException e) {
      System.out.printf("Unable to open link %s\n", link);
    } catch (LinkNotFoundException e) {
      System.out.printf("Link %s is not active\n", link);
    }
  }
}
