package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    private ReviewReactorRepository reviewReactorRepository;
    public ReviewHandler(ReviewReactorRepository reviewReactorRepository) {
        this.reviewReactorRepository = reviewReactorRepository;
    }


    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                /*.flatMap(review -> {
                    return reviewReactorRepository.save(review);
                })
                .flatMap(savedReview -> {
                    return ServerResponse.status(HttpStatus.OK).bodyValue(savedReview);
                })*/
                .flatMap(review -> reviewReactorRepository.save(review))
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId=request.queryParam("movieInfoId");
        Flux<Review> reviewsFlux;
        if(movieInfoId.isPresent()){
            reviewsFlux = reviewReactorRepository.findAllByMovieInfoId(Long.valueOf(movieInfoId.get()));
        }
        else{
            reviewsFlux = reviewReactorRepository.findAll();
        }
        return BuildReviewResponse(reviewsFlux);

    }

    private Mono<ServerResponse> BuildReviewResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {

        var reviewId=serverRequest.pathVariable("id");
        var existingReview=reviewReactorRepository.findById(reviewId);

        return existingReview
                .flatMap(review->serverRequest.bodyToMono(Review.class)
                        .map(review1 -> {
                            review.setComment(review1.getComment());
                            review.setRating(review1.getRating());
                            return review;
                        })
                )
                .flatMap(reviewReactorRepository::save)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {

        var reviewId=serverRequest.pathVariable("id");
        var existingReview=reviewReactorRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactorRepository.deleteById(reviewId))
                .then(ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }
}
