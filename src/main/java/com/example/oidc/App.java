package com.example.oidc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootApplication
@RestController
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    // Log all incoming requests
    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain)
                    throws ServletException, IOException {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("Incoming request: ")
                        .append(request.getMethod())
                        .append(" ")
                        .append(request.getRequestURI());
                if (request.getQueryString() != null) {
                    logMsg.append("?").append(request.getQueryString());
                }
                // Log client IP
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                logMsg.append(" | IP: ").append(ip);
                // Log all headers
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames.hasMoreElements()) {
                    logMsg.append(" | Headers: [");
                    boolean firstHeader = true;
                    while (headerNames.hasMoreElements()) {
                        String header = headerNames.nextElement();
                        if (!firstHeader)
                            logMsg.append(", ");
                        logMsg.append(header).append("=").append(request.getHeader(header));
                        firstHeader = false;
                    }
                    logMsg.append("]");
                }
                // Log all parameters
                java.util.Enumeration<String> paramNames = request.getParameterNames();
                if (paramNames.hasMoreElements()) {
                    logMsg.append(" | Params: [");
                    boolean first = true;
                    while (paramNames.hasMoreElements()) {
                        String param = paramNames.nextElement();
                        if (!first)
                            logMsg.append(", ");
                        logMsg.append(param).append("=").append(request.getParameter(param));
                        first = false;
                    }
                    logMsg.append("]");
                }
                log.info(logMsg.toString());
                filterChain.doFilter(request, response);
            }
        };
    }



    @Autowired
    public App() {
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    
}
