package com.example.demo.web;

import com.example.demo.model.Group;
import com.example.demo.model.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.ws.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class GroupController {

    private final Logger log = LoggerFactory.getLogger(GroupController.class);
    private GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/groups")
    Collection<Group> groups() {
        return groupRepository.findAll();
    }

    @GetMapping("/group/{id}")
    ResponseEntity<?> getGroup(@PathVariable Long id) {
        Optional<Group> group = groupRepository.findById(id);
        return group.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/group")
    ResponseEntity<Group> createGroup(@Valid @RequestBody Group group) throws URISyntaxException {
        log.info("Request to create group: {}", group);
        Group result = groupRepository.save(group);
        return ResponseEntity.created(new URI("/api/group/"+ result.getId()))
                .body(result);
    }

    @PutMapping("/group/{id}")
    ResponseEntity<Group> updateGroup(@PathVariable Long id, @Valid @RequestBody Group group) {
        log.info("Request to update group: {}", group);
        Optional<Group> repoGroup = groupRepository.findById(id);
        if(repoGroup.isPresent()) {
            Group update = repoGroup.get();
            update.setCity(group.getCity());
            update.setName(group.getName());
            update.setAddress(group.getAddress());
            update.setCountry(group.getCountry());
            update.setEvents(group.getEvents());
            update.setPostalCode(group.getPostalCode());
            update.setStateOrProvince(group.getStateOrProvince());
            update.setUser(group.getUser());

            groupRepository.save(repoGroup.get());

            return ResponseEntity.ok().body(repoGroup.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/group/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        log.info("Request to delete group: {}", id);
        if(groupRepository.findById(id).isPresent()) {
            groupRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
