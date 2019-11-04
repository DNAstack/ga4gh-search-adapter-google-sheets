package com.dnastack.search.sheets.search;

import com.dnastack.search.sheets.shared.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SearchController {

    @PostMapping("/api/search")
    public ResponseEntity search(){
        throw new NotFoundException("Search is not implemented in this API");
    }
}
