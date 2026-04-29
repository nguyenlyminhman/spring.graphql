# 🛡️ Role-Based Authorization với AOP trong Spring Boot + GraphQL

## 1. Thêm roles vào User model

```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "user_roles")
private Set<String> roles = new HashSet<>(Set.of("USER"));
```

---

## 2. Tạo annotation mới `@RequireRole`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();  // Ví dụ: @RequireRole("ADMIN") hoặc @RequireRole({"ADMIN","MOD"})
}
```

---

## 3. Tạo Aspect để kiểm tra role

```java
@Before("@annotation(requireRole)")
public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
    // Lấy user từ GraphQL context (giống RequireLoginAspect)
    User user = ctx.get(CURRENT_USER_KEY);

    if (user == null) {
        throw new UnauthorizedException("Chưa đăng nhập");
    }

    Set<String> required = Set.of(requireRole.value());

    // Nếu không có role phù hợp
    if (Collections.disjoint(user.getRoles(), required)) {
        throw new ForbiddenException("Cần role: " + required);
    }
}
```

---

## 4. Sử dụng trong Resolver

```java
@QueryMapping
@RequireLogin
@RequireRole("ADMIN")   // Chỉ ADMIN mới truy cập được
public List<User> allUsers(GraphQLContext ctx) {
    ...
}
```

---

## 💡 Ý tưởng chính

- `@RequireLogin` → đảm bảo user đã đăng nhập
- `@RequireRole` → kiểm tra quyền (authorization)
- AOP sẽ tự động intercept trước khi method chạy
- Giúp code **clean hơn**, không cần check role thủ công trong từng resolver

---

## 🚀 Gợi ý nâng cao

- Dùng `enum Role` thay vì `String` để tránh typo
- Cache roles trong JWT để giảm query DB
- Kết hợp với `Spring Security` để unify auth + authorization  
