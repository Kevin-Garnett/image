package com.hsbc.image;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

@Controller
//@EnableBinding(Source.class)
public class CommentController {

    //Non-Cloud version

    private final RabbitTemplate rabbitTemplate;

    private final MeterRegistry meterRegistry;

    public CommentController(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry){
        this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/comments")
    public Mono<String> addComment(Mono<Comment> newComment){
        return newComment.flatMap(
                comment -> Mono.fromRunnable(
                        () -> rabbitTemplate.convertAndSend(
                                "learning-spring-boot",
                                "comment.new",
                                comment
                        )
                ).then(Mono.just(comment))
        ).log("commentService-publish").flatMap(comment -> {
                    meterRegistry.counter("comment.produced", "imageId", comment.getImageId())
                            .increment();
                    return Mono.just("redirect:/");
                }
        );
    }


    //Cloud version:
    //private final CounterService counterService;
    /*
    private FluxSink<Message<Comment>> commentSink;
    private Flux<Message<Comment>> flux;

    public CommentController(){//CounterService counterService){
        //this.counterService = counterService;
        this.flux = Flux.<Message<Comment>>create(
                emitter -> this.commentSink = emitter,
                FluxSink.OverflowStrategy.IGNORE
        ).publish().autoConnect();
    }

    @PostMapping("/comment")
    public Mono<String> addComment(Mono<Comment> newComment){
        if (commentSink != null){
            return newComment.map(comment ->
                commentSink.next(MessageBuilder.withPayload(comment).build())
            ).then(Mono.just("redirect:/"));
        } else {
            return Mono.just("redirect:/");
        }
    }

    @StreamEmitter
    public void emit(@Output(Source.OUTPUT) FluxSender output){
        output.send(this.flux);
    }
    */
}
