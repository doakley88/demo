package com.example.demo;

import com.example.demo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * Used to use this to prep some demo code, now only keeping it around to log
 * when the server's running, and in case we need to do any real initialization in the future.
 */
@Component
public class Initializer implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(Initializer.class);


    @Autowired
    FileStorageService fss;

    public Initializer() {

    }

    @Override
    public void run (String... strings) {
        logger.info("System Initialized and ready to roll");

    }


}
