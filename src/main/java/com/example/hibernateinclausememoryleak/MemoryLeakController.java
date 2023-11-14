package com.example.hibernateinclausememoryleak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemoryLeakController {

    private static int numberOfParam = (2^15)-1;

    private final BookRepository bookRepository;


    @GetMapping("/queryIn/{iteration}")
    ResponseEntity<Void> queryIn(@PathVariable int iteration) {
        for (int i = 0; i < iteration; i++) {
            List<Integer> ids = randomList();
            log.info("iteration {} with collection size of {}", i, ids.size());
            this.bookRepository.getBooksById(ids);
        }
        return ResponseEntity.ok().build();
    }

    private List<Integer> randomList() {
        var rand = new Random();
        return IntStream.range(0, numberOfParam--).mapToObj(v -> rand.nextInt(Integer.MAX_VALUE)).toList();
    }
}
