package com.codex.backend.config;

import com.codex.backend.domain.user.User;
import com.codex.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void init() {
        long count = userRepository.count();

        if (count == 0) {
            // 自动创建默认管理员账号
            User admin = new User(
                    "admin@admin.com",
                    passwordEncoder.encode("12345678"),
                    "Admin User"
            );
            userRepository.save(admin);
            System.out.println("✅ 自动创建默认管理员账号: admin@admin.com / 12345678");
        } else {
            System.out.println("ℹ️ 已检测到现有用户, 跳过管理员初始化");
        }
    }
}
