package com.planwith.user.adapter.in.web.terms;

import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/terms")
public class TermsDocsController {

    private static final Set<String> ALLOWED_DOCS = Set.of("service", "privacy", "age", "marketing");

    @GetMapping(value = "/docs/{name}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getTermsDocument(@PathVariable String name) {
        if (!ALLOWED_DOCS.contains(name)) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        Resource resource = new ClassPathResource("static/terms/" + name + ".html");
        if (!resource.exists()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(resource);
    }
}
