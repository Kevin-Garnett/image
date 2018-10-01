package com.hsbc.person;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class PersonController {

    @PostMapping(value="/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Mono<Void> create(Flux<Person> persons) {
        System.out.println(persons);
        return persons.flatMap(
                person -> {
                    System.out.println(person);
                    Mono<List<Person>> personList = persons.collectList();
                    return Mono.when(personList);
                }
        ).then();
    }

    @GetMapping(value="/person")
    public Mono<String> index(final Model model){

        //Initialize 3 Person in Flux
        /*
        List<Person> list = new ArrayList<Person>();
        list.add(new Person("Kevin","1"));
        list.add(new Person("Sherry","2"));
        list.add(new Person("Gunter","3"));
        */
        final Flux<Person> persons = Flux.just(
                new Person("Kevin","1"),
                new Person("Sherry", "2"),
                new Person("Gunter", "3")
        );

        model.addAttribute("persons", persons);

        return Mono.just("person");
    }
}
