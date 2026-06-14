
# 博客后端 API 开发执行计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于前端API文档，开发完整的博客后端服务，使用Spring Boot + Java技术栈

**Architecture:** 采用分层架构（Controller → Service → Repository → Database），使用JWT进行认证，MySQL作为数据库，统一响应格式。

**Tech Stack:** 
- Java 21
- Spring Boot 3.2.x
- Spring Security 6.x
- MySQL 8.0+
- JWT (JJWT)
- Lombok
- Spring Data JPA

---

## 项目结构规划

```
backend/
├── src/main/java/com/example/blog/
│   ├── controller/          # REST API控制层
│   ├── service/             # 业务逻辑层
│   ├── repository/          # 数据访问层
│   ├── entity/              # JPA实体类
│   ├── dto/                 # 请求/响应DTO
│   ├── config/              # 配置类（Security、JWT、Web）
│   ├── exception/           # 异常处理
│   ├── util/                # 工具类
│   └── BlogApplication.java # 启动类
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   └── schema.sql           # 数据库初始化脚本
├── src/test/java/           # 测试类
└── pom.xml                  # Maven依赖管理
```

---

## 任务分解

### 任务1: 初始化Spring Boot项目

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/example/blog/BlogApplication.java`

- [ ] **Step 1: 创建pom.xml配置文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>blog</artifactId>
    <version>1.0.0</version>
    <name>blog-backend</name>
    <description>Blog Backend API</description>
    
    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.3</jjwt.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建application.yml配置**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: admin
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

# JWT配置
jwt:
  secret: blog-jwt-secret-key-256-bit-minimum-length-required-for-security
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天

# API配置
api:
  prefix: /api/v1
```

- [ ] **Step 3: 创建启动类**

```java
package com.example.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
```

- [ ] **Step 4: 验证项目结构**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务2: 创建通用响应结构和异常处理

**Files:**
- Create: `backend/src/main/java/com/example/blog/dto/Result.java`
- Create: `backend/src/main/java/com/example/blog/exception/BusinessException.java`
- Create: `backend/src/main/java/com/example/blog/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建统一响应类 Result.java**

```java
package com.example.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    private String traceId;
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    
    public static <T> Result<T> error(String message) {
        return error(-1, message);
    }
}
```

- [ ] **Step 2: 创建业务异常类**

```java
package com.example.blog.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = -1;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 3: 创建全局异常处理器**

```java
package com.example.blog.exception;

import com.example.blog.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseEntity.ok(Result.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常: {}", e.getMessage());
        return ResponseEntity.ok(Result.error(401, "未认证，请登录"));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("凭证错误: {}", e.getMessage());
        return ResponseEntity.ok(Result.error(401, "用户名或密码错误"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("参数校验失败: {}", errors);
        return ResponseEntity.ok(Result.success("参数校验失败", errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "系统内部错误"));
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务3: 创建用户实体和认证相关类

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/User.java`
- Create: `backend/src/main/java/com/example/blog/repository/UserRepository.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/LoginDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/RegisterDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/UserDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/LoginResponseDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/GuestLoginResponseDTO.java`

- [ ] **Step 1: 创建User实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(length = 100)
    private String displayName;
    
    @Column(length = 500)
    private String avatarUrl;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isGuest = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建UserRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

- [ ] **Step 3: 创建登录请求DTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 4: 创建注册请求DTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;
}
```

- [ ] **Step 5: 创建UserDTO响应**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private String id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String role;
    private Boolean isGuest;
    private String createdAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isGuest(user.getIsGuest())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null)
                .build();
    }
}
```

- [ ] **Step 6: 创建登录响应DTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    private String refreshToken;
    private UserDTO user;
}
```

- [ ] **Step 7: 创建访客登录响应DTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestLoginResponseDTO {
    
    private String token;
    private String userId;
    private String displayName;
    private String role;
    private String expiresAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static GuestLoginResponseDTO create(String token, String userId, String displayName) {
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        return GuestLoginResponseDTO.builder()
                .token(token)
                .userId(userId)
                .displayName(displayName)
                .role("GUEST")
                .expiresAt(expiresAt.format(FORMATTER))
                .build();
    }
}
```

- [ ] **Step 8: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务4: 创建JWT工具类和Security配置

**Files:**
- Create: `backend/src/main/java/com/example/blog/util/JwtUtil.java`
- Create: `backend/src/main/java/com/example/blog/config/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/example/blog/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/example/blog/config/WebConfig.java`

- [ ] **Step 1: 创建JwtUtil工具类**

```java
package com.example.blog.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
    
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, expiration);
    }
    
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, refreshExpiration);
    }
    
    private String createToken(Map<String, Object> claims, String subject, Long expireTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
```

- [ ] **Step 2: 创建JwtAuthenticationFilter**

```java
package com.example.blog.config;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String jwt = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String username = jwtUtil.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOptional = userRepository.findByUsername(username);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 3: 创建SecurityConfig**

```java
package com.example.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/guest-login",
                    "/api/v1/articles",
                    "/api/v1/articles/**",
                    "/api/v1/tags",
                    "/api/v1/articles/categories",
                    "/api/v1/questions",
                    "/api/v1/questions/**",
                    "/api/v1/about"
                ).permitAll()
                .requestMatchers("/api/v1/**").authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("User not found");
        };
    }
}
```

- [ ] **Step 4: 创建WebConfig跨域配置**

```java
package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 5: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务5: 创建认证服务和控制器

**Files:**
- Create: `backend/src/main/java/com/example/blog/service/AuthService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/AuthServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/AuthController.java`

- [ ] **Step 1: 创建AuthService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.LoginDTO;
import com.example.blog.dto.request.RegisterDTO;
import com.example.blog.dto.response.GuestLoginResponseDTO;
import com.example.blog.dto.response.LoginResponseDTO;
import com.example.blog.dto.response.UserDTO;

public interface AuthService {
    
    LoginResponseDTO login(LoginDTO dto);
    
    UserDTO register(RegisterDTO dto);
    
    UserDTO getCurrentUser(String username);
    
    void logout(String refreshToken);
    
    GuestLoginResponseDTO guestLogin();
    
    void deleteGuest(String userId);
}
```

- [ ] **Step 2: 创建AuthServiceImpl实现类**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.LoginDTO;
import com.example.blog.dto.request.RegisterDTO;
import com.example.blog.dto.response.GuestLoginResponseDTO;
import com.example.blog.dto.response.LoginResponseDTO;
import com.example.blog.dto.response.UserDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.AuthService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    @Transactional
    public LoginResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(UserDTO.fromEntity(user))
                .build();
    }
    
    @Override
    @Transactional
    public UserDTO register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .displayName(dto.getUsername())
                .role("USER")
                .isGuest(false)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getUsername());
        return UserDTO.fromEntity(savedUser);
    }
    
    @Override
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return UserDTO.fromEntity(user);
    }
    
    @Override
    @Transactional
    public void logout(String refreshToken) {
        // 在实际应用中，应该将refreshToken加入黑名单
        log.info("User logged out");
    }
    
    @Override
    @Transactional
    public GuestLoginResponseDTO guestLogin() {
        String guestUsername = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        String guestEmail = guestUsername + "@guest.com";
        
        User guest = User.builder()
                .username(guestUsername)
                .email(guestEmail)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .displayName("访客")
                .role("GUEST")
                .isGuest(true)
                .build();
        
        User savedGuest = userRepository.save(guest);
        String token = jwtUtil.generateToken(savedGuest.getUsername());
        
        log.info("Guest user created: {}", savedGuest.getId());
        return GuestLoginResponseDTO.create(token, savedGuest.getId(), savedGuest.getDisplayName());
    }
    
    @Override
    @Transactional
    public void deleteGuest(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        if (!user.getIsGuest()) {
            throw new BusinessException(403, "只能删除访客账号");
        }
        
        userRepository.delete(user);
        log.info("Guest user deleted: {}", userId);
    }
}
```

- [ ] **Step 3: 创建AuthController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.LoginDTO;
import com.example.blog.dto.request.RegisterDTO;
import com.example.blog.dto.response.GuestLoginResponseDTO;
import com.example.blog.dto.response.LoginResponseDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.dto.response.UserDTO;
import com.example.blog.entity.User;
import com.example.blog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponseDTO>> login(@Valid @RequestBody LoginDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PostMapping("/register")
    public ResponseEntity<Result<UserDTO>> register(@Valid @RequestBody RegisterDTO dto) {
        UserDTO response = authService.register(dto);
        return ResponseEntity.ok(Result.success("注册成功", response));
    }
    
    @GetMapping("/me")
    public ResponseEntity<Result<UserDTO>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserDTO response = authService.getCurrentUser(user.getUsername());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request != null ? request.getRefreshToken() : "");
        return ResponseEntity.ok(Result.success("退出成功", null));
    }
    
    @PostMapping("/guest-login")
    public ResponseEntity<Result<GuestLoginResponseDTO>> guestLogin() {
        GuestLoginResponseDTO response = authService.guestLogin();
        return ResponseEntity.ok(Result.success(response));
    }
    
    @DeleteMapping("/guest")
    public ResponseEntity<Result<Void>> deleteGuest(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        authService.deleteGuest(user.getId());
        return ResponseEntity.ok(Result.success("删除成功", null));
    }
    
    private static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务6: 创建文章相关实体和DTO

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/Article.java`
- Create: `backend/src/main/java/com/example/blog/entity/Category.java`
- Create: `backend/src/main/java/com/example/blog/entity/Tag.java`
- Create: `backend/src/main/java/com/example/blog/entity/ArticleTag.java`
- Create: `backend/src/main/java/com/example/blog/repository/ArticleRepository.java`
- Create: `backend/src/main/java/com/example/blog/repository/CategoryRepository.java`
- Create: `backend/src/main/java/com/example/blog/repository/TagRepository.java`
- Create: `backend/src/main/java/com/example/blog/repository/ArticleTagRepository.java`

- [ ] **Step 1: 创建Article实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "articles", indexes = {
    @Index(name = "idx_slug", columnList = "slug"),
    @Index(name = "idx_author", columnList = "author_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_published_at", columnList = "published_at")
})
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, length = 100)
    private String slug;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String excerpt;
    
    @Column(length = 500)
    private String coverImage;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";
    
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0;
    
    @Column(name = "author_id", nullable = false)
    private String authorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;
    
    @Column(name = "category_id")
    private String categoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
    
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArticleTag> articleTags = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Transient
    private List<String> tags;
    
    public void addTag(Tag tag) {
        ArticleTag articleTag = ArticleTag.builder()
                .article(this)
                .tag(tag)
                .build();
        articleTags.add(articleTag);
    }
    
    public void removeTag(Tag tag) {
        articleTags.removeIf(at -> at.getTag().equals(tag));
    }
}
```

- [ ] **Step 2: 创建Category实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name")
})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 创建Tag实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name")
})
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建ArticleTag关联实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article_tags", indexes = {
    @Index(name = "idx_at_article", columnList = "article_id"),
    @Index(name = "idx_at_tag", columnList = "tag_id")
})
public class ArticleTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
```

- [ ] **Step 5: 创建ArticleRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
    
    Optional<Article> findBySlug(String slug);
    
    Page<Article> findByStatus(String status, Pageable pageable);
    
    Page<Article> findByAuthorId(String authorId, Pageable pageable);
    
    Page<Article> findByCategoryId(String categoryId, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = :status AND " +
           "(a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR a.excerpt LIKE %:keyword%)")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, 
                                   @Param("status") String status, 
                                   Pageable pageable);
    
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount - 1 WHERE a.id = :id")
    void decrementLikeCount(@Param("id") String id);
    
    @Query("SELECT a FROM Article a JOIN a.articleTags at JOIN at.tag t WHERE t.name = :tagName AND a.status = :status")
    Page<Article> findByTagName(@Param("tagName") String tagName, 
                                @Param("status") String status, 
                                Pageable pageable);
    
    List<Article> findTop5ByStatusOrderByCreatedAtDesc(String status);
}
```

- [ ] **Step 6: 创建CategoryRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + 1 WHERE c.id = :id")
    void incrementArticleCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount - 1 WHERE c.id = :id")
    void decrementArticleCount(@Param("id") String id);
}
```

- [ ] **Step 7: 创建TagRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    
    Optional<Tag> findByName(String name);
    
    boolean existsByName(String name);
    
    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount + 1 WHERE t.id = :id")
    void incrementArticleCount(@Param("id") String id);
    
    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount - 1 WHERE t.id = :id")
    void decrementArticleCount(@Param("id") String id);
}
```

- [ ] **Step 8: 创建ArticleTagRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, String> {
    
    List<ArticleTag> findByArticleId(String articleId);
    
    @Modifying
    @Query("DELETE FROM ArticleTag at WHERE at.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") String articleId);
}
```

- [ ] **Step 9: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务7: 创建文章DTO和服务

**Files:**
- Create: `backend/src/main/java/com/example/blog/dto/request/CreateArticleDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/UpdateArticleDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/ArticleDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/ArticleListDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/ArticleService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/ArticleServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/ArticleController.java`

- [ ] **Step 1: 创建CreateArticleDTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleDTO {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String slug;
    
    private String excerpt;
    
    private String coverImage;
    
    @NotBlank(message = "状态不能为空")
    private String status;
    
    private String categoryId;
    
    private List<String> tags;
}
```

- [ ] **Step 2: 创建UpdateArticleDTO**

```java
package com.example.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleDTO {
    
    private String title;
    
    private String content;
    
    private String excerpt;
    
    private String coverImage;
    
    private String status;
    
    private String categoryId;
    
    private List<String> tags;
}
```

- [ ] **Step 3: 创建ArticleDTO**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    
    private String id;
    private String slug;
    private String title;
    private String content;
    private String excerpt;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String authorId;
    private String authorName;
    private String categoryId;
    private String categoryName;
    private List<String> tags;
    private String createdAt;
    private String updatedAt;
    private String publishedAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static ArticleDTO fromEntity(Article article) {
        List<String> tagNames = new ArrayList<>();
        if (article.getArticleTags() != null) {
            article.getArticleTags().forEach(at -> tagNames.add(at.getTag().getName()));
        }
        
        return ArticleDTO.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .content(article.getContent())
                .excerpt(article.getExcerpt())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .authorId(article.getAuthorId())
                .authorName(article.getAuthor() != null ? article.getAuthor().getDisplayName() : null)
                .categoryId(article.getCategoryId())
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .tags(tagNames)
                .createdAt(article.getCreatedAt() != null ? article.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt().format(FORMATTER) : null)
                .publishedAt(article.getPublishedAt() != null ? article.getPublishedAt().format(FORMATTER) : null)
                .build();
    }
}
```

- [ ] **Step 4: 创建ArticleListDTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleListDTO {
    
    private List<ArticleDTO> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer number;
    private Integer size;
}
```

- [ ] **Step 5: 创建ArticleService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.CreateArticleDTO;
import com.example.blog.dto.request.UpdateArticleDTO;
import com.example.blog.dto.response.ArticleDTO;
import com.example.blog.dto.response.ArticleListDTO;

import java.util.List;

public interface ArticleService {
    
    ArticleDTO createArticle(CreateArticleDTO dto, String authorId);
    
    ArticleDTO updateArticle(String id, UpdateArticleDTO dto);
    
    void deleteArticle(String id);
    
    ArticleDTO getArticleById(String id);
    
    ArticleDTO getArticleBySlug(String slug);
    
    ArticleListDTO getArticles(String categoryId, String tag, String keyword, 
                                String status, Integer page, Integer size);
    
    List<String> listCategories();
    
    String createCategory(String name);
    
    List<String> listTags();
    
    String createTag(String name);
}
```

- [ ] **Step 6: 创建ArticleServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.CreateArticleDTO;
import com.example.blog.dto.request.UpdateArticleDTO;
import com.example.blog.dto.response.ArticleDTO;
import com.example.blog.dto.response.ArticleListDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.ArticleTagRepository;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    
    @Override
    @Transactional
    public ArticleDTO createArticle(CreateArticleDTO dto, String authorId) {
        Article article = Article.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .excerpt(dto.getExcerpt())
                .coverImage(dto.getCoverImage())
                .status(dto.getStatus())
                .authorId(authorId)
                .categoryId(dto.getCategoryId())
                .build();
        
        if (dto.getSlug() != null && !dto.getSlug().isEmpty()) {
            article.setSlug(dto.getSlug());
        } else {
            article.setSlug(generateSlug(dto.getTitle()));
        }
        
        if ("PUBLISHED".equals(dto.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        if (dto.getCategoryId() != null) {
            categoryRepository.incrementArticleCount(dto.getCategoryId());
        }
        
        Article savedArticle = articleRepository.save(article);
        
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagName : dto.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                tag.setArticleCount(tag.getArticleCount() + 1);
                tagRepository.save(tag);
                
                ArticleTag articleTag = ArticleTag.builder()
                        .article(savedArticle)
                        .tag(tag)
                        .build();
                articleTagRepository.save(articleTag);
            }
        }
        
        log.info("Article created: {}", savedArticle.getId());
        return ArticleDTO.fromEntity(savedArticle);
    }
    
    @Override
    @Transactional
    public ArticleDTO updateArticle(String id, UpdateArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        if (dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
        }
        if (dto.getExcerpt() != null) {
            article.setExcerpt(dto.getExcerpt());
        }
        if (dto.getCoverImage() != null) {
            article.setCoverImage(dto.getCoverImage());
        }
        if (dto.getStatus() != null) {
            article.setStatus(dto.getStatus());
            if ("PUBLISHED".equals(dto.getStatus()) && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        
        if (dto.getCategoryId() != null) {
            String oldCategoryId = article.getCategoryId();
            if (!dto.getCategoryId().equals(oldCategoryId)) {
                if (oldCategoryId != null) {
                    categoryRepository.decrementArticleCount(oldCategoryId);
                }
                categoryRepository.incrementArticleCount(dto.getCategoryId());
            }
            article.setCategoryId(dto.getCategoryId());
        }
        
        if (dto.getTags() != null) {
            List<ArticleTag> existingTags = articleTagRepository.findByArticleId(id);
            List<String> existingTagNames = existingTags.stream()
                    .map(at -> at.getTag().getName())
                    .collect(Collectors.toList());
            
            for (ArticleTag at : existingTags) {
                if (!dto.getTags().contains(at.getTag().getName())) {
                    Tag tag = at.getTag();
                    tag.setArticleCount(tag.getArticleCount() - 1);
                    tagRepository.save(tag);
                }
            }
            
            articleTagRepository.deleteByArticleId(id);
            
            for (String tagName : dto.getTags()) {
                if (!existingTagNames.contains(tagName)) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    tag.setArticleCount(tag.getArticleCount() + 1);
                    tagRepository.save(tag);
                    
                    ArticleTag articleTag = ArticleTag.builder()
                            .article(article)
                            .tag(tag)
                            .build();
                    articleTagRepository.save(articleTag);
                }
            }
        }
        
        Article updatedArticle = articleRepository.save(article);
        log.info("Article updated: {}", id);
        return ArticleDTO.fromEntity(updatedArticle);
    }
    
    @Override
    @Transactional
    public void deleteArticle(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        if (article.getCategoryId() != null) {
            categoryRepository.decrementArticleCount(article.getCategoryId());
        }
        
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(id);
        for (ArticleTag at : articleTags) {
            Tag tag = at.getTag();
            tag.setArticleCount(tag.getArticleCount() - 1);
            tagRepository.save(tag);
        }
        
        articleRepository.delete(article);
        log.info("Article deleted: {}", id);
    }
    
    @Override
    public ArticleDTO getArticleById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        articleRepository.incrementViewCount(id);
        return ArticleDTO.fromEntity(article);
    }
    
    @Override
    public ArticleDTO getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        articleRepository.incrementViewCount(article.getId());
        return ArticleDTO.fromEntity(article);
    }
    
    @Override
    public ArticleListDTO getArticles(String categoryId, String tag, String keyword, 
                                       String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Article> articlePage;
        
        if (keyword != null && !keyword.isEmpty()) {
            articlePage = articleRepository.searchByKeyword(keyword, 
                    status != null ? status : "PUBLISHED", pageable);
        } else if (categoryId != null) {
            articlePage = articleRepository.findByCategoryId(categoryId, pageable);
        } else if (tag != null) {
            articlePage = articleRepository.findByTagName(tag, 
                    status != null ? status : "PUBLISHED", pageable);
        } else {
            articlePage = articleRepository.findByStatus(status != null ? status : "PUBLISHED", pageable);
        }
        
        List<ArticleDTO> content = articlePage.getContent().stream()
                .map(ArticleDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ArticleListDTO.builder()
                .content(content)
                .totalElements(articlePage.getTotalElements())
                .totalPages(articlePage.getTotalPages())
                .number(articlePage.getNumber())
                .size(articlePage.getSize())
                .build();
    }
    
    @Override
    public List<String> listCategories() {
        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public String createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException("分类已存在");
        }
        
        Category category = Category.builder()
                .name(name)
                .build();
        categoryRepository.save(category);
        log.info("Category created: {}", name);
        return name;
    }
    
    @Override
    public List<String> listTags() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public String createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new BusinessException("标签已存在");
        }
        
        Tag tag = Tag.builder()
                .name(name)
                .build();
        tagRepository.save(tag);
        log.info("Tag created: {}", name);
        return name;
    }
    
    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug + "-" + System.currentTimeMillis();
    }
}
```

- [ ] **Step 7: 创建ArticleController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.CreateArticleDTO;
import com.example.blog.dto.request.UpdateArticleDTO;
import com.example.blog.dto.response.ArticleDTO;
import com.example.blog.dto.response.ArticleListDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.entity.User;
import com.example.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    @PostMapping("/articles")
    public ResponseEntity<Result<ArticleDTO>> createArticle(
            @Valid @RequestBody CreateArticleDTO dto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ArticleDTO response = articleService.createArticle(dto, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PutMapping("/articles/{id}")
    public ResponseEntity<Result<ArticleDTO>> updateArticle(
            @PathVariable String id,
            @RequestBody UpdateArticleDTO dto) {
        ArticleDTO response = articleService.updateArticle(id, dto);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Result<Void>> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }
    
    @GetMapping("/articles/{id}")
    public ResponseEntity<Result<ArticleDTO>> getArticleById(@PathVariable String id) {
        ArticleDTO response = articleService.getArticleById(id);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/articles/slug/{slug}")
    public ResponseEntity<Result<ArticleDTO>> getArticleBySlug(@PathVariable String slug) {
        ArticleDTO response = articleService.getArticleBySlug(slug);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/articles")
    public ResponseEntity<Result<ArticleListDTO>> getArticles(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        ArticleListDTO response = articleService.getArticles(categoryId, tag, keyword, status, page, size);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/articles/categories")
    public ResponseEntity<Result<List<String>>> listCategories() {
        List<String> response = articleService.listCategories();
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PostMapping("/categories")
    public ResponseEntity<Result<String>> createCategory(@RequestBody CategoryRequest request) {
        String response = articleService.createCategory(request.getName());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/tags")
    public ResponseEntity<Result<List<String>>> listTags() {
        List<String> response = articleService.listTags();
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PostMapping("/tags")
    public ResponseEntity<Result<String>> createTag(@RequestBody TagRequest request) {
        String response = articleService.createTag(request.getName());
        return ResponseEntity.ok(Result.success(response));
    }
    
    private static class CategoryRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    private static class TagRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
```

- [ ] **Step 8: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务8: 创建评论模块

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/Comment.java`
- Create: `backend/src/main/java/com/example/blog/repository/CommentRepository.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/CreateCommentDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/CommentDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/CommentListDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/CommentService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/CommentServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/CommentController.java`

- [ ] **Step 1: 创建Comment实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", indexes = {
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "article_id", nullable = false)
    private String articleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "parent_id")
    private String parentId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "APPROVED";
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Comment parent;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建CommentRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    
    Page<Comment> findByArticleIdAndParentIdIsNull(String articleId, Pageable pageable);
    
    List<Comment> findByParentId(String parentId);
    
    long countByArticleId(String articleId);
}
```

- [ ] **Step 3: 创建CreateCommentDTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {
    
    @NotBlank(message = "文章ID不能为空")
    private String articleId;
    
    private String parentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
```

- [ ] **Step 4: 创建CommentDTO**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    private String id;
    private String articleId;
    private String userId;
    private String userName;
    private String parentId;
    private String content;
    private String status;
    private List<CommentDTO> replies;
    private String createdAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static CommentDTO fromEntity(Comment comment) {
        List<CommentDTO> replies = new ArrayList<>();
        if (comment.getReplies() != null) {
            replies = comment.getReplies().stream()
                    .map(CommentDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return CommentDTO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .userName(comment.getUser() != null ? comment.getUser().getDisplayName() : null)
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .status(comment.getStatus())
                .replies(replies)
                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().format(FORMATTER) : null)
                .build();
    }
}
```

- [ ] **Step 5: 创建CommentListDTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListDTO {
    
    private List<CommentDTO> content;
    private Integer totalPages;
    private Long totalElements;
}
```

- [ ] **Step 6: 创建CommentService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.CreateCommentDTO;
import com.example.blog.dto.response.CommentDTO;
import com.example.blog.dto.response.CommentListDTO;

import java.util.List;

public interface CommentService {
    
    CommentDTO createComment(CreateCommentDTO dto, String userId);
    
    CommentListDTO getComments(String articleId, Integer page, Integer size);
    
    List<CommentDTO> getReplies(String commentId);
    
    void deleteComment(String id);
}
```

- [ ] **Step 7: 创建CommentServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.CreateCommentDTO;
import com.example.blog.dto.response.CommentDTO;
import com.example.blog.dto.response.CommentListDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    
    @Override
    @Transactional
    public CommentDTO createComment(CreateCommentDTO dto, String userId) {
        Article article = articleRepository.findById(dto.getArticleId())
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        
        Comment comment = Comment.builder()
                .articleId(dto.getArticleId())
                .userId(userId)
                .parentId(dto.getParentId())
                .content(dto.getContent())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        article.setCommentCount(article.getCommentCount() + 1);
        articleRepository.save(article);
        
        log.info("Comment created: {}", savedComment.getId());
        return CommentDTO.fromEntity(savedComment);
    }
    
    @Override
    public CommentListDTO getComments(String articleId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByArticleIdAndParentIdIsNull(articleId, pageable);
        
        List<CommentDTO> content = commentPage.getContent().stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
        
        return CommentListDTO.builder()
                .content(content)
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .build();
    }
    
    @Override
    public List<CommentDTO> getReplies(String commentId) {
        List<Comment> replies = commentRepository.findByParentId(commentId);
        return replies.stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        
        Article article = articleRepository.findById(comment.getArticleId()).orElse(null);
        if (article != null) {
            article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
            articleRepository.save(article);
        }
        
        commentRepository.delete(comment);
        log.info("Comment deleted: {}", id);
    }
}
```

- [ ] **Step 8: 创建CommentController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.CreateCommentDTO;
import com.example.blog.dto.response.CommentDTO;
import com.example.blog.dto.response.CommentListDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.entity.User;
import com.example.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    public ResponseEntity<Result<CommentDTO>> createComment(
            @Valid @RequestBody CreateCommentDTO dto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CommentDTO response = commentService.createComment(dto, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping
    public ResponseEntity<Result<CommentListDTO>> getComments(
            @RequestParam String articleId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        CommentListDTO response = commentService.getComments(articleId, page, size);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Result<List<CommentDTO>>> getReplies(@PathVariable String commentId) {
        List<CommentDTO> response = commentService.getReplies(commentId);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }
}
```

- [ ] **Step 9: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务9: 创建点赞模块

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/Reaction.java`
- Create: `backend/src/main/java/com/example/blog/repository/ReactionRepository.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/ReactionDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/ReactionService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/ReactionServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/ReactionController.java`

- [ ] **Step 1: 创建Reaction实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reactions", indexes = {
    @Index(name = "idx_article_reaction", columnList = "article_id,user_id")
})
public class Reaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "article_id", nullable = false)
    private String articleId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建ReactionRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
    
    Optional<Reaction> findByArticleIdAndUserId(String articleId, String userId);
    
    long countByArticleId(String articleId);
    
    boolean existsByArticleIdAndUserId(String articleId, String userId);
    
    @Modifying
    @Query("DELETE FROM Reaction r WHERE r.articleId = :articleId AND r.userId = :userId")
    void deleteByArticleIdAndUserId(@Param("articleId") String articleId, @Param("userId") String userId);
}
```

- [ ] **Step 3: 创建ReactionDTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    
    private Boolean liked;
    private Integer likeCount;
}
```

- [ ] **Step 4: 创建ReactionService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.response.ReactionDTO;

public interface ReactionService {
    
    ReactionDTO toggleReaction(String articleId, String userId);
    
    ReactionDTO getReactionStatus(String articleId, String userId);
}
```

- [ ] **Step 5: 创建ReactionServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.response.ReactionDTO;
import com.example.blog.entity.Reaction;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.ReactionRepository;
import com.example.blog.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {
    
    private final ReactionRepository reactionRepository;
    private final ArticleRepository articleRepository;
    
    @Override
    @Transactional
    public ReactionDTO toggleReaction(String articleId, String userId) {
        if (!articleRepository.existsById(articleId)) {
            throw new BusinessException(404, "文章不存在");
        }
        
        Optional<Reaction> existing = reactionRepository.findByArticleIdAndUserId(articleId, userId);
        
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            articleRepository.decrementLikeCount(articleId);
            long count = reactionRepository.countByArticleId(articleId);
            return ReactionDTO.builder()
                    .liked(false)
                    .likeCount((int) count)
                    .build();
        } else {
            Reaction reaction = Reaction.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .build();
            reactionRepository.save(reaction);
            articleRepository.incrementLikeCount(articleId);
            long count = reactionRepository.countByArticleId(articleId);
            return ReactionDTO.builder()
                    .liked(true)
                    .likeCount((int) count)
                    .build();
        }
    }
    
    @Override
    public ReactionDTO getReactionStatus(String articleId, String userId) {
        long count = reactionRepository.countByArticleId(articleId);
        boolean liked = reactionRepository.existsByArticleIdAndUserId(articleId, userId);
        
        return ReactionDTO.builder()
                .liked(liked)
                .likeCount((int) count)
                .build();
    }
}
```

- [ ] **Step 6: 创建ReactionController**

```java
package com.example.blog.controller;

import com.example.blog.dto.response.ReactionDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.entity.User;
import com.example.blog.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ReactionController {
    
    private final ReactionService reactionService;
    
    @PostMapping("/{articleId}/reaction")
    public ResponseEntity<Result<ReactionDTO>> toggleReaction(
            @PathVariable String articleId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReactionDTO response = reactionService.toggleReaction(articleId, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/{articleId}/reaction")
    public ResponseEntity<Result<ReactionDTO>> getReactionStatus(
            @PathVariable String articleId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReactionDTO response = reactionService.getReactionStatus(articleId, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
}
```

- [ ] **Step 7: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务10: 创建问答模块

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/Question.java`
- Create: `backend/src/main/java/com/example/blog/entity/Answer.java`
- Create: `backend/src/main/java/com/example/blog/repository/QuestionRepository.java`
- Create: `backend/src/main/java/com/example/blog/repository/AnswerRepository.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/CreateQuestionDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/CreateAnswerDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/QuestionDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/QuestionListDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/AnswerDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/QuestionService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/QuestionServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/QuestionController.java`

- [ ] **Step 1: 创建Question实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_is_public", columnList = "is_public")
})
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";
    
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建Answer实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "answers", indexes = {
    @Index(name = "idx_question_id", columnList = "question_id"),
    @Index(name = "idx_answer_user_id", columnList = "user_id"),
    @Index(name = "idx_is_accepted", columnList = "is_accepted")
})
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "question_id", nullable = false)
    private String questionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "APPROVED";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: 创建QuestionRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    
    Page<Question> findByStatus(String status, Pageable pageable);
    
    Page<Question> findByIsPublic(Boolean isPublic, Pageable pageable);
    
    Page<Question> findByStatusAndIsPublic(String status, Boolean isPublic, Pageable pageable);
    
    Page<Question> findByUserId(String userId, Pageable pageable);
}
```

- [ ] **Step 4: 创建AnswerRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {
    
    List<Answer> findByQuestionId(String questionId);
    
    long countByQuestionId(String questionId);
    
    @Modifying
    @Query("UPDATE Answer a SET a.isAccepted = :isAccepted WHERE a.id = :id")
    void updateAcceptedStatus(@Param("id") String id, @Param("isAccepted") Boolean isAccepted);
    
    @Modifying
    @Query("UPDATE Answer a SET a.isAccepted = false WHERE a.question.id = :questionId AND a.isAccepted = true")
    void clearAcceptedAnswer(@Param("questionId") String questionId);
}
```

- [ ] **Step 5: 创建CreateQuestionDTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDTO {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private Boolean isPublic = true;
}
```

- [ ] **Step 6: 创建CreateAnswerDTO**

```java
package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnswerDTO {
    
    @NotBlank(message = "回答内容不能为空")
    private String content;
}
```

- [ ] **Step 7: 创建QuestionDTO**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    
    private String id;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private String status;
    private Boolean isPublic;
    private AnswerDTO[] answers;
    private String createdAt;
    private String updatedAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static QuestionDTO fromEntity(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .userId(question.getUserId())
                .userName(question.getUser() != null ? question.getUser().getDisplayName() : null)
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus())
                .isPublic(question.getIsPublic())
                .createdAt(question.getCreatedAt() != null ? question.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(question.getUpdatedAt() != null ? question.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }
}
```

- [ ] **Step 8: 创建QuestionListDTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListDTO {
    
    private List<QuestionDTO> content;
    private Long totalElements;
    private Integer totalPages;
}
```

- [ ] **Step 9: 创建AnswerDTO**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    
    private String id;
    private String questionId;
    private String userId;
    private String userName;
    private String content;
    private Boolean isAccepted;
    private String status;
    private String createdAt;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public static AnswerDTO fromEntity(Answer answer) {
        return AnswerDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestionId())
                .userId(answer.getUserId())
                .userName(answer.getUser() != null ? answer.getUser().getDisplayName() : null)
                .content(answer.getContent())
                .isAccepted(answer.getIsAccepted())
                .status(answer.getStatus())
                .createdAt(answer.getCreatedAt() != null ? answer.getCreatedAt().format(FORMATTER) : null)
                .build();
    }
}
```

- [ ] **Step 10: 创建QuestionService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.CreateAnswerDTO;
import com.example.blog.dto.request.CreateQuestionDTO;
import com.example.blog.dto.response.AnswerDTO;
import com.example.blog.dto.response.QuestionDTO;
import com.example.blog.dto.response.QuestionListDTO;

public interface QuestionService {
    
    QuestionDTO createQuestion(CreateQuestionDTO dto, String userId);
    
    QuestionDTO getQuestion(String id);
    
    QuestionListDTO getQuestions(String status, Boolean isPublic, Integer page, Integer size);
    
    void closeQuestion(String id, String userId);
    
    AnswerDTO createAnswer(String questionId, CreateAnswerDTO dto, String userId);
    
    void acceptAnswer(String questionId, String answerId, String userId);
}
```

- [ ] **Step 11: 创建QuestionServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.CreateAnswerDTO;
import com.example.blog.dto.request.CreateQuestionDTO;
import com.example.blog.dto.response.AnswerDTO;
import com.example.blog.dto.response.QuestionDTO;
import com.example.blog.dto.response.QuestionListDTO;
import com.example.blog.entity.Answer;
import com.example.blog.entity.Question;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.AnswerRepository;
import com.example.blog.repository.QuestionRepository;
import com.example.blog.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    
    @Override
    @Transactional
    public QuestionDTO createQuestion(CreateQuestionDTO dto, String userId) {
        Question question = Question.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true)
                .build();
        
        Question savedQuestion = questionRepository.save(question);
        log.info("Question created: {}", savedQuestion.getId());
        return QuestionDTO.fromEntity(savedQuestion);
    }
    
    @Override
    public QuestionDTO getQuestion(String id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "问题不存在"));
        
        QuestionDTO dto = QuestionDTO.fromEntity(question);
        
        List<Answer> answers = answerRepository.findByQuestionId(id);
        AnswerDTO[] answerDTOs = answers.stream()
                .map(AnswerDTO::fromEntity)
                .toArray(AnswerDTO[]::new);
        dto.setAnswers(answerDTOs);
        
        return dto;
    }
    
    @Override
    public QuestionListDTO getQuestions(String status, Boolean isPublic, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Question> questionPage;
        
        if (status != null && isPublic != null) {
            questionPage = questionRepository.findByStatusAndIsPublic(status, isPublic, pageable);
        } else if (status != null) {
            questionPage = questionRepository.findByStatus(status, pageable);
        } else if (isPublic != null) {
            questionPage = questionRepository.findByIsPublic(isPublic, pageable);
        } else {
            questionPage = questionRepository.findAll(pageable);
        }
        
        List<QuestionDTO> content = questionPage.getContent().stream()
                .map(QuestionDTO::fromEntity)
                .collect(Collectors.toList());
        
        return QuestionListDTO.builder()
                .content(content)
                .totalElements(questionPage.getTotalElements())
                .totalPages(questionPage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional
    public void closeQuestion(String id, String userId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "问题不存在"));
        
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能关闭自己的问题");
        }
        
        question.setStatus("CLOSED");
        questionRepository.save(question);
        log.info("Question closed: {}", id);
    }
    
    @Override
    @Transactional
    public AnswerDTO createAnswer(String questionId, CreateAnswerDTO dto, String userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(404, "问题不存在"));
        
        if ("CLOSED".equals(question.getStatus())) {
            throw new BusinessException("问题已关闭，不能回答");
        }
        
        Answer answer = Answer.builder()
                .questionId(questionId)
                .userId(userId)
                .content(dto.getContent())
                .build();
        
        Answer savedAnswer = answerRepository.save(answer);
        log.info("Answer created: {}", savedAnswer.getId());
        return AnswerDTO.fromEntity(savedAnswer);
    }
    
    @Override
    @Transactional
    public void acceptAnswer(String questionId, String answerId, String userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(404, "问题不存在"));
        
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能采纳自己问题的回答");
        }
        
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(404, "回答不存在"));
        
        answerRepository.clearAcceptedAnswer(questionId);
        answer.setIsAccepted(true);
        answerRepository.save(answer);
        
        question.setStatus("CLOSED");
        questionRepository.save(question);
        
        log.info("Answer accepted: {}", answerId);
    }
}
```

- [ ] **Step 12: 创建QuestionController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.CreateAnswerDTO;
import com.example.blog.dto.request.CreateQuestionDTO;
import com.example.blog.dto.response.AnswerDTO;
import com.example.blog.dto.response.QuestionDTO;
import com.example.blog.dto.response.QuestionListDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.entity.User;
import com.example.blog.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    
    @PostMapping
    public ResponseEntity<Result<QuestionDTO>> createQuestion(
            @Valid @RequestBody CreateQuestionDTO dto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        QuestionDTO response = questionService.createQuestion(dto, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<QuestionDTO>> getQuestion(@PathVariable String id) {
        QuestionDTO response = questionService.getQuestion(id);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @GetMapping
    public ResponseEntity<Result<QuestionListDTO>> getQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        QuestionListDTO response = questionService.getQuestions(status, isPublic, page, size);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PutMapping("/{id}/close")
    public ResponseEntity<Result<Void>> closeQuestion(
            @PathVariable String id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        questionService.closeQuestion(id, user.getId());
        return ResponseEntity.ok(Result.success("问题已关闭", null));
    }
    
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<Result<AnswerDTO>> createAnswer(
            @PathVariable String questionId,
            @Valid @RequestBody CreateAnswerDTO dto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        AnswerDTO response = questionService.createAnswer(questionId, dto, user.getId());
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PutMapping("/{questionId}/answers/{answerId}/accept")
    public ResponseEntity<Result<Void>> acceptAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        questionService.acceptAnswer(questionId, answerId, user.getId());
        return ResponseEntity.ok(Result.success("采纳成功", null));
    }
}
```

- [ ] **Step 13: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务11: 创建用户资料模块

**Files:**
- Create: `backend/src/main/java/com/example/blog/dto/request/UpdateProfileDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/ProfileDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/ProfileService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/ProfileServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/ProfileController.java`

- [ ] **Step 1: 创建UpdateProfileDTO**

```java
package com.example.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {
    
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String website;
    private String github;
    private String twitter;
    private String about;
}
```

- [ ] **Step 2: 创建ProfileDTO**

```java
package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    
    private String userId;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String website;
    private String github;
    private String twitter;
    private String about;
}
```

- [ ] **Step 3: 创建ProfileService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.UpdateProfileDTO;
import com.example.blog.dto.response.ProfileDTO;

public interface ProfileService {
    
    ProfileDTO getProfile(String userId);
    
    ProfileDTO updateProfile(String userId, UpdateProfileDTO dto);
}
```

- [ ] **Step 4: 创建ProfileServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.UpdateProfileDTO;
import com.example.blog.dto.response.ProfileDTO;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    
    private final UserRepository userRepository;
    
    @Override
    public ProfileDTO getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        return ProfileDTO.builder()
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .bio("")
                .avatarUrl(user.getAvatarUrl())
                .website("")
                .github("")
                .twitter("")
                .about("")
                .build();
    }
    
    @Override
    @Transactional
    public ProfileDTO updateProfile(String userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        User savedUser = userRepository.save(user);
        log.info("Profile updated: {}", userId);
        
        return ProfileDTO.builder()
                .userId(savedUser.getId())
                .displayName(savedUser.getDisplayName())
                .bio(dto.getBio())
                .avatarUrl(savedUser.getAvatarUrl())
                .website(dto.getWebsite())
                .github(dto.getGithub())
                .twitter(dto.getTwitter())
                .about(dto.getAbout())
                .build();
    }
}
```

- [ ] **Step 5: 创建ProfileController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.UpdateProfileDTO;
import com.example.blog.dto.response.ProfileDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.entity.User;
import com.example.blog.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ProfileService profileService;
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<Result<ProfileDTO>> getProfile(@PathVariable String userId) {
        ProfileDTO response = profileService.getProfile(userId);
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PutMapping("/{userId}/profile")
    public ResponseEntity<Result<ProfileDTO>> updateProfile(
            @PathVariable String userId,
            @RequestBody UpdateProfileDTO dto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(userId)) {
            return ResponseEntity.ok(Result.error(403, "只能修改自己的资料"));
        }
        ProfileDTO response = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(Result.success(response));
    }
}
```

- [ ] **Step 6: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务12: 创建关于模块

**Files:**
- Create: `backend/src/main/java/com/example/blog/entity/About.java`
- Create: `backend/src/main/java/com/example/blog/repository/AboutRepository.java`
- Create: `backend/src/main/java/com/example/blog/dto/request/UpdateAboutDTO.java`
- Create: `backend/src/main/java/com/example/blog/dto/response/AboutDTO.java`
- Create: `backend/src/main/java/com/example/blog/service/AboutService.java`
- Create: `backend/src/main/java/com/example/blog/service/impl/AboutServiceImpl.java`
- Create: `backend/src/main/java/com/example/blog/controller/AboutController.java`

- [ ] **Step 1: 创建About实体**

```java
package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "about")
public class About {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String skills;
    
    @Column(columnDefinition = "TEXT")
    private String socialLinks;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建AboutRepository**

```java
package com.example.blog.repository;

import com.example.blog.entity.About;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AboutRepository extends JpaRepository<About, String> {
    
    Optional<About> findFirstByOrderByCreatedAtDesc();
}
```

- [ ] **Step 3: 创建UpdateAboutDTO**

```java
package com.example.blog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAboutDTO {
    
    private String content;
    private List<String> skills;
    private String socialLinks;
}
```

- [ ] **Step 4: 创建AboutDTO**

```java
package com.example.blog.dto.response;

import com.example.blog.entity.About;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AboutDTO {
    
    private String content;
    private List<String> skills;
    private String socialLinks;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static AboutDTO fromEntity(About about) {
        List<String> skills = List.of();
        if (about.getSkills() != null && !about.getSkills().isEmpty()) {
            try {
                skills = objectMapper.readValue(about.getSkills(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                skills = Arrays.asList(about.getSkills().split(","));
            }
        }
        
        return AboutDTO.builder()
                .content(about.getContent())
                .skills(skills)
                .socialLinks(about.getSocialLinks())
                .build();
    }
}
```

- [ ] **Step 5: 创建AboutService接口**

```java
package com.example.blog.service;

import com.example.blog.dto.request.UpdateAboutDTO;
import com.example.blog.dto.response.AboutDTO;

public interface AboutService {
    
    AboutDTO getAbout();
    
    AboutDTO updateAbout(UpdateAboutDTO dto);
}
```

- [ ] **Step 6: 创建AboutServiceImpl**

```java
package com.example.blog.service.impl;

import com.example.blog.dto.request.UpdateAboutDTO;
import com.example.blog.dto.response.AboutDTO;
import com.example.blog.entity.About;
import com.example.blog.repository.AboutRepository;
import com.example.blog.service.AboutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {
    
    private final AboutRepository aboutRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public AboutDTO getAbout() {
        Optional<About> aboutOptional = aboutRepository.findFirstByOrderByCreatedAtDesc();
        
        if (aboutOptional.isEmpty()) {
            About defaultAbout = About.builder()
                    .content("欢迎来到我的博客")
                    .skills("[]")
                    .socialLinks("{}")
                    .build();
            aboutRepository.save(defaultAbout);
            return AboutDTO.fromEntity(defaultAbout);
        }
        
        return AboutDTO.fromEntity(aboutOptional.get());
    }
    
    @Override
    @Transactional
    public AboutDTO updateAbout(UpdateAboutDTO dto) {
        Optional<About> aboutOptional = aboutRepository.findFirstByOrderByCreatedAtDesc();
        
        About about = aboutOptional.orElseGet(() -> About.builder().build());
        
        if (dto.getContent() != null) {
            about.setContent(dto.getContent());
        }
        if (dto.getSkills() != null) {
            try {
                about.setSkills(objectMapper.writeValueAsString(dto.getSkills()));
            } catch (Exception e) {
                about.setSkills(String.join(",", dto.getSkills()));
            }
        }
        if (dto.getSocialLinks() != null) {
            about.setSocialLinks(dto.getSocialLinks());
        }
        
        About savedAbout = aboutRepository.save(about);
        log.info("About updated");
        return AboutDTO.fromEntity(savedAbout);
    }
}
```

- [ ] **Step 7: 创建AboutController**

```java
package com.example.blog.controller;

import com.example.blog.dto.request.UpdateAboutDTO;
import com.example.blog.dto.response.AboutDTO;
import com.example.blog.dto.response.Result;
import com.example.blog.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/about")
@RequiredArgsConstructor
public class AboutController {
    
    private final AboutService aboutService;
    
    @GetMapping
    public ResponseEntity<Result<AboutDTO>> getAbout() {
        AboutDTO response = aboutService.getAbout();
        return ResponseEntity.ok(Result.success(response));
    }
    
    @PutMapping
    public ResponseEntity<Result<AboutDTO>> updateAbout(@RequestBody UpdateAboutDTO dto) {
        AboutDTO response = aboutService.updateAbout(dto);
        return ResponseEntity.ok(Result.success(response));
    }
}
```

- [ ] **Step 8: 编译验证**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

---

### 任务13: 创建数据库初始化脚本

**Files:**
- Create: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: 创建schema.sql**

```sql
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    article_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    article_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(36) PRIMARY KEY,
    slug VARCHAR(100) UNIQUE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    excerpt TEXT,
    cover_image VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    author_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at DATETIME,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS article_tags (
    id VARCHAR(36) PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    tag_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(36) PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    parent_id VARCHAR(36),
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES comments(id)
);

CREATE TABLE IF NOT EXISTS reactions (
    id VARCHAR(36) PRIMARY KEY,
    article_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_reaction (article_id, user_id)
);

CREATE TABLE IF NOT EXISTS questions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS answers (
    id VARCHAR(36) PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS about (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT,
    skills TEXT,
    social_links TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_comments_article ON comments(article_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_reactions_article_user ON reactions(article_id, user_id);
```

- [ ] **Step 2: 更新application.yml添加SQL初始化配置**

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

---

### 任务14: 测试运行

**Files:**
- 运行项目测试

- [ ] **Step 1: 启动项目**

Run: `cd backend && mvn spring-boot:run`
Expected: 项目启动成功，监听8080端口

- [ ] **Step 2: 测试接口**

```bash
# 注册用户
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "email": "test@example.com", "password": "123456"}'

# 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "123456"}'

# 创建文章
curl -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"title": "测试文章", "content": "内容", "status": "PUBLISHED"}'

# 获取文章列表
curl http://localhost:8080/api/v1/articles
```

---

## 执行方式选择

**Plan complete and saved to `backend-plan.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
