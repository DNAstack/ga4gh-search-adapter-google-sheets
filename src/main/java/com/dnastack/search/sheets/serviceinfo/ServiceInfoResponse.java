package com.dnastack.search.sheets.serviceinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfoResponse {
    /**
     * Service version.
     */
    String version;

    /**
     * Service name.
     */
    String title;

    /**
     * Service description.
     */
    String description;

    /**
     * Maintainer contact info.
     */
    Map<String, Object> contact;

    /**
     * License information for the exposed API.
     */
    Map<String, Object> license;
}
