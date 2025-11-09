package ru.vovandiya.linkshortener.sheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.service.LinkService;

@Component
@RequiredArgsConstructor
public class SchedulerTasks {
  private final LinkService linkService;

  @Scheduled(cron = "${app.deleteLinksCron}")
  public void deleteLinksTask() {
    linkService
        .deleteAllExpiredLinks()
        .forEach(link -> System.out.printf("[INFO] Link %s is expired!\n", link.getShortLink()));
  }
}
