package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
public class ReviewRestClient {

    private WebClient webClient;

    public ReviewRestClient(WebClient webClient){
        this.webClient=webClient;
    }

    @Value("${restClient.reviewUrl}")
    private String reviewUrl;

    public Flux<Review> retriveReviews(String movieId){
        var url= UriComponentsBuilder.fromHttpUrl(reviewUrl)
                        .queryParam("movieInfoId",movieId)
                                .buildAndExpand()
                                        .toString();
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log();
    }

}
