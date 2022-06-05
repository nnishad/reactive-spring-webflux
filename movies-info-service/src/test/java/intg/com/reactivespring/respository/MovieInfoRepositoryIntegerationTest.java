package com.reactivespring.respository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
class MovieInfoRepositoryIntegerationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp(){
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        movieInfoRepository.deleteAll().block();
    }
    @Test
    void findAll(){
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById(){
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc");

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises",movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo(){
        MovieInfo movieInfo=new MovieInfo(null, "Batman Begins 2",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2015-06-15"));

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals("Batman Begins 2 ",movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){

        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);
        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals(2021,movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void delete(){
        movieInfoRepository.deleteById("abc").block();
        var movieInfoFlux=movieInfoRepository.findAll().log();
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear(){
        var movieInfoFlux=movieInfoRepository.findByYear(2005);

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

}