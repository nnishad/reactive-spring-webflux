package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.respository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
class MoviesInfoControllerIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;
    @Autowired
    WebTestClient webTestClient;

    static private String MOVIE_INFO_URL="/v1/movieinfos/";

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos)
                .blockLast();
    }

    @Test
    void addMovieInfos() {
        var movieInfo=new MovieInfo(null, "Batman Begins123",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));


        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo=movieInfoEntityExchangeResult.getResponseBody();
                    assert  savedMovieInfo!=null;
                    assert  savedMovieInfo.getMovieInfoId()!=null;
                })
        ;
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getAllMovieInfos() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        var movieInfoId="abc";
        webTestClient.get()
                .uri(MOVIE_INFO_URL+"{id}",movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                /*.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo=movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo!=null;
                })*/
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("Dark Knight Rises")
                ;
    }

    @Test
    void updateMovieInfos() {
        var movieInfo=new MovieInfo(null, "Dark Knight Rises2",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var movieInfoId="abc";

        webTestClient.put()
                .uri(MOVIE_INFO_URL+"{id}",movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo=movieInfoEntityExchangeResult.getResponseBody();
                    assert  updatedMovieInfo!=null;
                    assert  updatedMovieInfo.getMovieInfoId()!=null;
                    assertEquals(updatedMovieInfo.getName(),movieInfo.getName());
                })
        ;
    }

    @Test
    void deleteMovieInfoById() {
        var movieInfoId="abc";
        webTestClient.delete()
                .uri(MOVIE_INFO_URL+"{id}",movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent()
        ;
    }

    @Test
    void getMovieInfosByYear() {
        var uri=UriComponentsBuilder.fromUriString(MOVIE_INFO_URL)
                        .queryParam("year",2005)
                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }
}