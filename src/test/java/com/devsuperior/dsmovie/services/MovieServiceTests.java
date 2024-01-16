package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository repository;

	private MovieEntity movieEntity;
	private MovieDTO movieDTO;
	private PageImpl<MovieEntity> page;
	private String movieTitle;
	private Long movieIdExists, movieIdNomExists, movieDependentId;

	@BeforeEach
	void setUp(){

		movieIdExists = 1L;
		movieIdNomExists = 2L;
		movieDependentId = 3L;

		movieTitle = "Test Movie";
		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movieEntity);

		Pageable pageable = PageRequest.of(0,12);
		page = new PageImpl<MovieEntity>(List.of(movieEntity));

		Mockito.when(repository.searchByTitle(movieTitle,pageable)).thenReturn(page);

		Mockito.when(repository.findById(movieIdExists)).thenReturn(Optional.of(movieEntity));
		Mockito.when(repository.findById(movieIdNomExists)).thenReturn(Optional.empty());

		Mockito.when(repository.getReferenceById(movieIdExists)).thenReturn(movieEntity);
		Mockito.when(repository.getReferenceById(movieIdNomExists)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.save(any())).thenReturn(movieEntity);

		Mockito.when(repository.existsById(movieIdExists)).thenReturn(true);
		Mockito.when(repository.existsById(movieDependentId)).thenReturn(true);
		Mockito.when(repository.existsById(movieIdNomExists)).thenReturn(false);

		Mockito.doNothing().when(repository).deleteById(movieIdExists);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(movieDependentId);
	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Pageable pageable = PageRequest.of(0,12);
		Page<MovieDTO> result = service.findAll(movieTitle, pageable);

		Assertions.assertNotNull(result);
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(movieIdExists);
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () ->{
			MovieDTO result = service.findById(movieIdNomExists);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(movieEntity.getId(), result.getId());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result =  service.update(movieIdExists, movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(movieEntity.getId(), result.getId());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () ->{
			MovieDTO result = service.update(movieIdNomExists, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(movieIdExists);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(movieIdNomExists);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(movieDependentId);
		});
	}
}
