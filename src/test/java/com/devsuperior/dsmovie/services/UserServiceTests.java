package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository repository;

	@Mock
	private CustomUserUtil customUserUtil;

	private String userNameExists, userNameNonExists;

	private List<UserDetailsProjection> userDetailsProjection;


	@BeforeEach
	void setUp(){
		userNameExists = "maria@gmail.com";
		userNameNonExists = "bob@gmail.com";

		UserEntity userLogged = UserFactory.createCustomUserEntity(userNameExists);

		userDetailsProjection = UserDetailsFactory.createCustomAdminClientUser(userNameExists);
		Mockito.when(repository.findByUsername(userNameExists)).thenReturn(Optional.of(userLogged));
		Mockito.when(repository.findByUsername(userNameNonExists)).thenReturn(Optional.empty());

		Mockito.when(repository.searchUserAndRolesByUsername(userNameExists)).thenReturn(userDetailsProjection);
		Mockito.when(repository.searchUserAndRolesByUsername(userNameNonExists)).thenReturn(new ArrayList<>());
	}

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
		Mockito.when(customUserUtil.getLoggedUsername()).thenReturn(userNameExists);

		UserEntity result = service.authenticated();
		Assertions.assertNotNull(result);
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Mockito.doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();

		Assertions.assertThrows(UsernameNotFoundException.class, ()-> {
			service.authenticated();
		});
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
		UserDetails result = service.loadUserByUsername(userNameExists);
		Assertions.assertNotNull(result);
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Assertions.assertThrows(UsernameNotFoundException.class, () ->{
			service.loadUserByUsername(userNameNonExists);
		});
	}
}
