package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageFileRepository extends JpaRepository<ImageFile, Long> {
    ImageFile findByName(String name);
}
