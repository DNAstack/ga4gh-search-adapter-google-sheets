package com.dnastack.search.sheets.serviceinfo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServiceInfoController {

    public static final String SEARCH_VERSION = "prototype";

    @Value("${ga4gh.service-info.title}")
    private String serviceTitle;

    @Value("${ga4gh.service-info.description}")
    private String serviceDescription;

    @GetMapping({"/ga4gh/search/v1/service-info", "/"})
    public ServiceInfoResponse getServiceInfo() {
        return new ServiceInfoResponse(
                SEARCH_VERSION,
                serviceTitle,
                serviceDescription,
                Map.of(
                        "company", "DNAstack",
                        "email", "info@dnastack.com"
                ),
                Map.of(
                        "license", "Copyright 2019 DNAstack"
                )
        );
    }
}
