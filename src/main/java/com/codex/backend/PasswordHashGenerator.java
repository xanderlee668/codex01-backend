package com.codex.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        // 创建 BCrypt 加密器（默认强度 10）
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 要加密的明文密码
        String rawPassword = "12345678";

        // 生成哈希
        String hashed = encoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("新生成的哈希: " + hashed);
        System.out.println("\n✅ 请将此哈希复制到 H2 控制台:");
        System.out.println("UPDATE users SET password_hash = '" + hashed + "' WHERE email = 'admin@admin.com';");
    }
}
