package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactorRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.isA;


@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
public class ReviewsUnitTest {
    @MockBean
    private ReviewReactorRepository reviewReactorRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String Review_URL="/v1/reviews";

    @Test
    void addReview(){
        var review=new Review(null, 1L, "Awesome Movie", 9.0);
        Mockito.when(reviewReactorRepository.save((isA(Review.class)))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
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
    void addReview_validation(){
        var review=new Review(null, null, "Awesome Movie", -9.0);
        Mockito.when(reviewReactorRepository.save((isA(Review.class)))).thenReturn(Mono.just(new Review("abc", null, "Awesome Movie", -9.0)));
        webTestClient.post()
                .uri(Review_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

}
