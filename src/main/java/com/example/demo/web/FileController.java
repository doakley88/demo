package com.example.demo.web;

import com.example.demo.exception.MyFileNotFoundException;
import com.example.demo.model.ImageFile;
import com.example.demo.model.ImageFileRepository;
import com.example.demo.payload.UploadFileResponse;
import com.example.demo.service.FileStorageService;
import com.example.demo.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private ImageFileRepository repository;

    public FileController(ImageFileRepository repo) {
        this.repository = repo;
    }

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file")MultipartFile file) {
        logger.info("got the file! " + file.getOriginalFilename());
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = getDownloadUri(fileName);

        repository.save(new ImageFile(fileName, fileDownloadUri));

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        //load file as resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        //try to determine content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type");
            //default content type
            contentType = DEFAULT_CONTENT_TYPE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ resource.getFilename() + "\"")
                .body(resource);

    }

    @GetMapping("/list")
    Collection<ImageFile> list() {
        ArrayList<ImageFile> files = new ArrayList<>();
        List<Resource> resources = fileStorageService.loadResources();
        resources.forEach(resource -> {
            files.add(new ImageFile(resource.getFilename(), getDownloadUri(resource.getFilename()) ));
        });

        return files;
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<?> deleteImage(@PathVariable String filename) {
        logger.info("request to delete file: " + filename);
        boolean success = fileStorageService.deleteFile(filename);
        if(success) {
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/compose")
    public ResponseEntity<Resource> composeImage(@RequestParam("base") String baseFile,
                                                 @RequestParam("mapper") String mapperFile,
                                                 @RequestParam("resultName") String resultName,
                                                 HttpServletRequest request) {
        logger.info("request to remake '"+baseFile+"' with the components of '" +mapperFile +"'" +
                " and store it as '" +resultName+"'");
        BufferedImage base;
        BufferedImage mapper;
        try {
            base = ImageIO.read(fileStorageService.loadFileAsResource(baseFile).getFile());
            mapper = ImageIO.read(fileStorageService.loadFileAsResource(mapperFile).getFile());

        } catch (MyFileNotFoundException ex) {
            logger.info("Couldn't find one or more of the files the files "+ ex.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException iox) {
            logger.info("IOException " + iox.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BufferedImage result = ImageUtils.remakeImage(mapper, base, 5, 5);
        String filename = fileStorageService.storeFile(result, resultName);
        String fileDownloadUri = getDownloadUri(filename);
        logger.info("downloadUri is " + fileDownloadUri);

        repository.save(new ImageFile(filename, fileDownloadUri));

        Resource resource = fileStorageService.loadFileAsResource(filename);
        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type");
            //default content type
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ resource.getFilename() + "\"")
                .body(resource);




    }

    private String getDownloadUri(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

    }

}
