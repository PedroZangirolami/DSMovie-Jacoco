package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

	@Mock
	private ScoreRepository repository;

	@Mock UserService userService;

	@Mock
	MovieRepository movieRepository;

	private UserEntity userLogged;
	private Long existingMovieId, nomExistingMovieId;
	private MovieEntity movieEntity;
	private MovieDTO movieDTO;
	private ScoreDTO scoreDTO;
	private ScoreEntity score;

	@BeforeEach
	void setUp(){

		existingMovieId = 1L;
		nomExistingMovieId = 2L;

		userLogged = UserFactory.createUserEntity();
		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movieEntity);

		score = new ScoreEntity();
		score.setMovie(movieEntity);
		score.setUser(userLogged);
		score.setValue(3.0);
		scoreDTO =  new ScoreDTO(score);
		movieEntity.getScores().add(score);

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nomExistingMovieId)).thenReturn(Optional.empty());
		Mockito.when(movieRepository.save(movieEntity)).thenReturn(movieEntity);
		Mockito.when(repository.saveAndFlush(score)).thenReturn(score);
	}
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {
		Mockito.when(userService.authenticated()).thenReturn(userLogged);

		MovieDTO result = service.saveScore(scoreDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), movieDTO.getId());
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId(){
		Mockito.when(userService.authenticated()).thenReturn(userLogged);

		ScoreDTO scoreDTO =  new ScoreDTO(2L, 3.0);

		Assertions.assertThrows(ResourceNotFoundException.class, () ->{
			MovieDTO result = service.saveScore(scoreDTO);
		});

	}
}
