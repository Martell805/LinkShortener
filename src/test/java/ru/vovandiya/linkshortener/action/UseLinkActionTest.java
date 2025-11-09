package ru.vovandiya.linkshortener.action;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vovandiya.linkshortener.exception.LinkNotFoundException;
import ru.vovandiya.linkshortener.service.LinkService;

@ExtendWith(MockitoExtension.class)
class UseLinkActionTest {

  @Mock private LinkService linkService;

  @InjectMocks private UseLinkAction useLinkAction;

  private void setScannerInput(String input) {
    try {
      InputStream inputStream = new ByteArrayInputStream(input.getBytes());
      Scanner scanner = new Scanner(inputStream);

      Field scannerField = UseLinkAction.class.getDeclaredField("scanner");
      scannerField.setAccessible(true);
      scannerField.set(useLinkAction, scanner);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set scanner input", e);
    }
  }

  @Test
  void getName_ShouldReturnCorrectName() {
    assert "Use link".equals(useLinkAction.getName());
  }

  @Test
  void run_ValidLink_ShouldUseLinkSuccessfully() throws Exception {
    // Given
    String link = "abc123";
    setScannerInput(link + "\n");

    // When
    useLinkAction.run();

    // Then
    verify(linkService).useLink(link);
  }

  @Test
  void run_LinkNotFound_ShouldPrintErrorMessage() throws Exception {
    // Given
    String link = "nonexistent";
    setScannerInput(link + "\n");
    doThrow(new LinkNotFoundException()).when(linkService).useLink(link);

    // When
    useLinkAction.run();

    // Then
    verify(linkService).useLink(link);
  }

  @Test
  void run_URISyntaxException_ShouldPrintErrorMessage() throws Exception {
    // Given
    String link = "invalid-link";
    setScannerInput(link + "\n");
    doThrow(new URISyntaxException("input", "reason")).when(linkService).useLink(link);

    // When
    useLinkAction.run();

    // Then
    verify(linkService).useLink(link);
  }

  @Test
  void run_IOException_ShouldPrintErrorMessage() throws Exception {
    // Given
    String link = "abc123";
    setScannerInput(link + "\n");
    doThrow(new IOException("IO error")).when(linkService).useLink(link);

    // When
    useLinkAction.run();

    // Then
    verify(linkService).useLink(link);
  }
}
