package com.example.demo.service;

import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.MyFileNotFoundException;
import com.example.demo.property.FileStorageProperties;
import com.example.demo.util.ImageUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will live.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        //Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        InputStream in = null;
        log.info("input; filename is " + fileName);
        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Invalid path sequence in filename " + fileName);
            }
            in = file.getInputStream();

            //Copy file to the target location, overridding if necessary
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException iox) {
            throw new FileStorageException("Could not store file " + fileName, iox);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String storeFile(BufferedImage image, String filename) {
        log.info("input filename is " + filename);
        String fileName = StringUtils.cleanPath(filename);


        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try {
            ImageIO.write(image, "jpg", targetLocation.toFile());
            return fileName;
        } catch (IOException iox) {
            log.info("IOException in storeFile " + iox.getMessage());
            throw new FileStorageException("FSE " + iox);
        }

    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);

        }
    }

    public boolean deleteFile(String fileName) {
        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Invalid path sequence in filename " + fileName);
            }
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            Files.delete(filePath);
            return true;
        } catch (IOException iox) {
            log.info("Trouble deleting file " +fileName);
        }
        return false;
    }

    public List<Resource> loadResources() {
        ArrayList<Resource> resources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(this.fileStorageLocation.toString()))){
            paths.filter(Files::isRegularFile)
                    .forEach( path -> {
                        try {
                            Resource resource = new UrlResource(path.toUri());
                            if (resource.exists()) {
                                resources.add(resource);
                            }
                        } catch (MalformedURLException ex) {
                            throw new MyFileNotFoundException("File not found", ex);
                        }
                    });
        } catch (IOException iox) {
            throw new MyFileNotFoundException("File not found ", iox);
        }
        return resources;
    }
}
