package ru.vovandiya.linkshortener.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vovandiya.linkshortener.entity.User;
import ru.vovandiya.linkshortener.exception.UserNotFoundException;
import ru.vovandiya.linkshortener.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() throws Exception {
    resetCurrentUser();

    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setName("Test User");
  }

  private void setCurrentUser(User user) throws Exception {
    Field currentUserField = UserService.class.getDeclaredField("currentUser");
    currentUserField.setAccessible(true);
    currentUserField.set(null, user);
  }

  private User getCurrentUser() throws Exception {
    Field currentUserField = UserService.class.getDeclaredField("currentUser");
    currentUserField.setAccessible(true);
    return (User) currentUserField.get(null);
  }

  private void resetCurrentUser() throws Exception {
    setCurrentUser(null);
  }

  @Test
  void getCurrentUser_WhenNoUserLoggedIn_ShouldReturnNull() throws Exception {
    // Given - currentUser is null after setUp()

    // When
    User result = userService.getCurrentUser();

    // Then
    assertNull(result);
    assertNull(getCurrentUser());
  }

  @Test
  void getCurrentUser_WhenUserLoggedIn_ShouldReturnUser() throws Exception {
    // Given
    setCurrentUser(testUser);

    // When
    User result = userService.getCurrentUser();

    // Then
    assertNotNull(result);
    assertEquals(testUser, result);
    assertEquals(testUser, getCurrentUser());
  }

  @Test
  void login_ValidUUID_ShouldSetCurrentUser() throws Exception {
    // Given
    when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

    // When
    User result = userService.login(testUser.getId());

    // Then
    assertNotNull(result);
    assertEquals(testUser, result);
    assertEquals(testUser, getCurrentUser());
    verify(userRepository).findById(testUser.getId());
  }

  @Test
  void login_InvalidUUID_ShouldThrowException() throws Exception {
    // Given
    UUID invalidUuid = UUID.randomUUID();
    when(userRepository.findById(invalidUuid)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(UserNotFoundException.class, () -> userService.login(invalidUuid));
    verify(userRepository).findById(invalidUuid);
    assertNull(getCurrentUser());
  }

  @Test
  void register_ValidName_ShouldCreateAndSetCurrentUser() throws Exception {
    // Given
    String userName = "New User";
    User newUser = new User();
    newUser.setId(UUID.randomUUID());
    newUser.setName(userName);

    when(userRepository.save(any(User.class))).thenReturn(newUser);

    // When
    User result = userService.register(userName);

    // Then
    assertNotNull(result);
    assertEquals(newUser, result);
    assertEquals(newUser, getCurrentUser());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void logout_ShouldClearCurrentUser() throws Exception {
    // Given
    setCurrentUser(testUser);

    // When
    userService.logout();

    // Then
    assertNull(getCurrentUser());
  }

  @Test
  void logout_WhenNoUserLoggedIn_ShouldNotFail() throws Exception {
    // Given - currentUser is null after setUp()

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> userService.logout());
    assertNull(getCurrentUser());
  }

  @Test
  void login_AfterLogout_ShouldSetNewCurrentUser() throws Exception {
    // Given
    setCurrentUser(testUser);
    userService.logout();

    User newUser = new User();
    newUser.setId(UUID.randomUUID());
    newUser.setName("Another User");

    when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));

    // When
    User result = userService.login(newUser.getId());

    // Then
    assertNotNull(result);
    assertEquals(newUser, result);
    assertEquals(newUser, getCurrentUser());
    assertNotEquals(testUser, getCurrentUser());
  }

  @Test
  void register_AfterLogin_ShouldReplaceCurrentUser() throws Exception {
    // Given
    setCurrentUser(testUser);

    User newUser = new User();
    newUser.setId(UUID.randomUUID());
    newUser.setName("New Registered User");

    when(userRepository.save(any(User.class))).thenReturn(newUser);

    // When
    User result = userService.register("New Registered User");

    // Then
    assertNotNull(result);
    assertEquals(newUser, result);
    assertEquals(newUser, getCurrentUser());
    assertNotEquals(testUser, getCurrentUser());
  }
}
