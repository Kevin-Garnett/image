package com.hsbc.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value="/demo")
public class ImageController {

    private Logger log = LoggerFactory.getLogger(ImageController.class);

    @GetMapping("/images")
    Flux<Image> images() {
        return Flux.just(
                new Image("1", "kevin.jpg"),
                new Image("2", "sherry.jpg"),
                new Image("3", "gunter.jpg")
        );
    }

    @PostMapping("/images")
    Mono<Void> create(@RequestBody Flux<Image> images) {
        return images.map(
                image -> {
                    log.info("We will save: " + image + " to a reactive database soon!");
                    return image;
                }
        ).then();
    }
}
