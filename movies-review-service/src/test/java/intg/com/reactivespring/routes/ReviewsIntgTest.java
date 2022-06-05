package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactorRepository reviewReactorRepository;

    static String Review_URL="/v1/reviews";

    @BeforeEach
    void setUp(){
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactorRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        reviewReactorRepository.deleteAll().block();
    }

    @Test
    void addReview(){
        var review=new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient.post()
                .uri(Review_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview=reviewEntityExchangeResult.getResponseBody();
                     assert savedReview!=null;
                    assert Objects.equals(savedReview.getRating(), review.getRating());
                });

    }

    @Test
    void getReviews(){
        webTestClient.get()
                .uri(Review_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);


    }
}
