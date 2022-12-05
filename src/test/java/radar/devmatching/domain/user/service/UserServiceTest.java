package radar.devmatching.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import radar.devmatching.common.exception.InvalidAccessException;
import radar.devmatching.domain.user.entity.User;
import radar.devmatching.domain.user.exception.DuplicateException;
import radar.devmatching.domain.user.repository.UserRepository;
import radar.devmatching.domain.user.service.dto.request.CreateUserRequest;
import radar.devmatching.domain.user.service.dto.request.UpdateUserRequest;
import radar.devmatching.domain.user.service.dto.response.UserResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService의")
public class UserServiceTest {

	static final Long TEST_USER_ID = 1L;
	static final Long TEST_USER_ID_EX = 2L;

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserServiceImpl(userRepository, passwordEncoder);
	}

	private User createUser() {
		return User.builder()
			.id(TEST_USER_ID)
			.username("username")
			.password("password")
			.nickName("nickName")
			.schoolName("schoolName")
			.githubUrl("githubUrl")
			.introduce("introduce")
			.build();
	}

	@Nested
	@DisplayName("createUser 메서드에서")
	class CreateUserMethod {

		@Test
		@DisplayName("예외를 던지지 않고 user가 저장된다.")
		public void createUserWithoutException() {
			//given
			CreateUserRequest request = getCreateUserRequest();
			User user = CreateUserRequest.toEntity(request);
			given(passwordEncoder.encode(any())).willReturn(request.getPassword());

			//when
			UserResponse saveUser = userService.createUser(request);

			//then
			assertThat(saveUser).usingRecursiveComparison().isEqualTo(UserResponse.of(user));
		}

		@Test
		@DisplayName("username 중복되면 예외를 던진다.")
		public void throwDuplicateExceptionAboutUsername() {
			//given
			CreateUserRequest request = getCreateUserRequest();
			given(userRepository.findByUsername(any())).willReturn(Optional.of(createUser()));
			//when
			//then
			assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(DuplicateException.class);
		}

		@Test
		@DisplayName("nickName 중복되면 예외를 던진다.")
		public void throwDuplicateExceptionAboutNickName() {
			//given
			CreateUserRequest request = getCreateUserRequest();
			given(userRepository.findByNickName(any())).willReturn(Optional.of(createUser()));
			//when
			//then
			assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(DuplicateException.class);
		}

		private CreateUserRequest getCreateUserRequest() {
			return CreateUserRequest.builder()
				.username("username")
				.password("password")
				.nickName("nickName")
				.schoolName("schoolName")
				.build();
		}
	}

	@Nested
	@DisplayName("getUser 메서드에서")
	class GetUserMethod {

		@Test
		@DisplayName("예외를 던지지 않고 User 정보를 가져온다.")
		public void getUserWithoutException() {
			//given
			User authUser = createUser();
			//when
			UserResponse getUser = userService.getUser(TEST_USER_ID, authUser);
			//then
			assertThat(getUser).usingRecursiveComparison().isEqualTo(UserResponse.of(authUser));
		}

		@Test
		@DisplayName("요청 userId와 사용자 userId가 달라 예외를 던진다.")
		public void requestUserIdNotEqualAuthUserID() {
			//given
			User authUser = createUser();
			//when
			//then
			assertThatThrownBy(() -> userService.getUser(TEST_USER_ID_EX, authUser))
				.isInstanceOf(InvalidAccessException.class);
		}

	}

	@Nested
	@DisplayName("updateUser 메서드에서")
	class UpdateUserMethod {

		@Test
		@DisplayName("예외를 던지지 않고 User 정보를 변경한다.")
		public void updateUserWithoutException() {
			//given
			User user = createUser();
			UpdateUserRequest request = UpdateUserRequest.builder()
				.nickName("updateNickName")
				.schoolName("updateSchoolName")
				.githubUrl("updateGithubUrl")
				.introduce("updateIntroduce")
				.build();
			given(userRepository.findByNickName(any())).willReturn(Optional.empty());
			//when
			UserResponse userResponse = userService.updateUser(request, TEST_USER_ID, user);
			//then
			assertThat(userResponse.getNickName()).isEqualTo(request.getNickName());
			assertThat(userResponse.getSchoolName()).isEqualTo(request.getSchoolName());
			assertThat(userResponse.getGithubUrl()).isEqualTo(request.getGithubUrl());
			assertThat(userResponse.getIntroduce()).isEqualTo(request.getIntroduce());
		}

		@Test
		@DisplayName("요청 userId와 사용자 userId가 달라 예외를 던진다.")
		public void requestUserIdNotEqualAuthUserID() {
			//given
			User user = createUser();
			UpdateUserRequest request = UpdateUserRequest.builder()
				.nickName("updateNickName")
				.schoolName("updateSchoolName")
				.githubUrl("updateGithubUrl")
				.introduce("updateIntroduce")
				.build();
			//when
			//then
			assertThatThrownBy(() -> userService.updateUser(request, TEST_USER_ID_EX, user))
				.isInstanceOf(InvalidAccessException.class);
		}

		@Test
		@DisplayName("NickName 중복되면 예외를 던진다.")
		public void throwDuplicateExceptionAboutNickName() {
			//given
			User user = createUser();
			User findUser = User.builder()
				.id(TEST_USER_ID_EX)
				.nickName("updateNickName")
				.build();
			UpdateUserRequest request = UpdateUserRequest.builder()
				.nickName("updateNickName")
				.schoolName("updateSchoolName")
				.githubUrl("updateGithubUrl")
				.introduce("updateIntroduce")
				.build();
			given(userRepository.findByNickName(request.getNickName())).willReturn(Optional.of(findUser));
			//when
			//then
			assertThatThrownBy(() -> userService.updateUser(request, TEST_USER_ID, user))
				.isInstanceOf(DuplicateException.class);
		}
	}

	@Nested
	@DisplayName("deleteUser 메서드에서")
	class DeleteUserMethod {

		@Test
		@DisplayName("정상적으로 user가 삭제된다.")
		public void deleteUserWithoutException() {
			//given
			User user = createUser();
			//when
			userService.deleteUser(TEST_USER_ID, user);
			//then
			verify(userRepository, times(1)).delete(user);
		}

		@Test
		@DisplayName("요청 userId와 사용자 userId가 달라 예외를 던진다.")
		public void requestUserIdNotEqualAuthUserID() {
			//given
			User authUser = createUser();
			//when
			//then
			assertThatThrownBy(() -> userService.deleteUser(TEST_USER_ID_EX, authUser))
				.isInstanceOf(InvalidAccessException.class);
			verify(userRepository, never()).delete(authUser);
		}
	}
}

