package ru.vovandiya.linkshortener.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.exception.UserNotFoundException;
import ru.vovandiya.linkshortener.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  private static User currentUser;

  public User getCurrentUser() {
    return currentUser;
  }

  public User login(UUID uuid) {
    currentUser = userRepository.findById(uuid).orElseThrow(UserNotFoundException::new);
    return currentUser;
  }

  public User register(String name) {
    currentUser = userRepository.save(User.builder().name(name).build());
    return currentUser;
  }

  public void logout() {
    currentUser = null;
  }
}
