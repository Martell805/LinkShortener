package ru.vovandiya.linkshortener.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.LinkService;
import ru.vovandiya.linkshortener.service.UserService;

@Component
@RequiredArgsConstructor
public class GetMyLinksAction implements Action {
  private final UserService userService;
  private final LinkService linkService;

  @Override
  public String getName() {
    return "Get my links";
  }

  @Override
  public void run() {
    System.out.println("Your links are:");
    linkService
        .getAllForUser(userService.getCurrentUser())
        .forEach(
            link -> {
              System.out.printf("\t%s\n", link.getShortLink());
              System.out.printf("\t\tTimes used: %s\n", link.getTimesUsed());
              System.out.printf("\t\tMax uses: %s\n", link.getMaxUses());
              System.out.printf("\t\tExpires in: %s\n", link.getExpiration());
              System.out.printf("\t\tDestination: %s\n", link.getDestination());
            });
  }
}
