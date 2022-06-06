package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MovieInfoRestClient {

    private WebClient webClient;

    public MovieInfoRestClient(WebClient webClient){
        this.webClient=webClient;
    }

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;


    public Mono<MovieInfo> retriveMovieInfo(String movieId){
        var url=moviesInfoUrl.concat("/{id}");

        return webClient.get()
                .uri(url,movieId)
                .retrieve()
                .bodyToMono(MovieInfo.class)
                .log();
    }

}
