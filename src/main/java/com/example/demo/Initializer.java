package com.example.demo;

import com.example.demo.exception.MyFileNotFoundException;
import com.example.demo.model.*;
import com.example.demo.property.ContextProperties;
import com.example.demo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class Initializer implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(Initializer.class);
    private final GroupRepository groupRepository;

    @Autowired
    FileStorageService fss;

    public Initializer(GroupRepository gr) {
        this.groupRepository = gr;
    }

    @Override
    public void run (String... strings) {
        Stream.of("Denver JUG", "Utah JUG", "Seattle JUG", "Richmond JUG")
                .forEach(name ->
                        groupRepository.save(new Group(name))
                );

        Group djug = groupRepository.findByName("Denver JUG");
        Event e = Event.builder().title("Full Stack Reactive")
                .description("Reactive with Spring Boot + React")
                .date(Instant.parse("2018-12-12T18:00:00.000Z"))
                .build();

        djug.setEvents(Collections.singleton(e));
        groupRepository.save(djug);

        groupRepository.findAll().forEach(System.out::println);

    }


}
