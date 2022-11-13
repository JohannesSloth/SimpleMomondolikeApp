package com.example.simplemomondolikeapp.controllers;

import com.example.simplemomondolikeapp.dto.Age;
import com.example.simplemomondolikeapp.dto.Gender;
import com.example.simplemomondolikeapp.dto.InfoResponse;
import com.example.simplemomondolikeapp.dto.Nationality;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class InfoController {

    @GetMapping(path = "/{name}")
    public InfoResponse getInfo(@PathVariable String name){
        Mono<Gender> gender = getGenderForName(name);
        Mono<Age> age = getAgeForName(name);
        Mono<Nationality> nationality = getNationalityForName(name);

        var resultMono = Mono.zip(gender, age, nationality).map(t -> {
            InfoResponse ir = new InfoResponse();

            ir.setGender(t.getT1().getGender());
            ir.setGenderProbability(t.getT1().getProbability());

            ir.setAge(t.getT2().getAge());
            ir.setAgeCount(t.getT2().getCount());

            ir.setCountry(t.getT3().getCountry().get(0).getCountry_id());
            ir.setCountryProbability(t.getT3().getCountry().get(0).getProbability());

            return ir;
        });
        InfoResponse result = resultMono.block();
        result.setName(name);

        return result;
    }

    Mono<Gender> getGenderForName(String name) {
        WebClient client = WebClient.create();
        Mono<Gender> gender = client.get()
                .uri("https://api.genderize.io?name=" + name)
                .retrieve()
                .bodyToMono(Gender.class);
        return gender;
    }

    Mono<Age> getAgeForName(String name) {
        WebClient client = WebClient.create();
        Mono<Age> age = client.get().uri("https://api.agify.io?name=" + name).retrieve().bodyToMono(Age.class);
        return age;
    }

    public Mono<Nationality> getNationalityForName(String name){
        WebClient client = WebClient.create();
        Mono<Nationality> nationality = client.get().uri("https://api.nationalize.io?name=" + name).retrieve().bodyToMono(Nationality.class);
        return nationality;
    }
}
