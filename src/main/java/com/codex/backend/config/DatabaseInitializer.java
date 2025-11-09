package com.codex.backend.config;

import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.listing.ListingCondition;
import com.codex.backend.domain.listing.TradeOption;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时初始化必要的用户与示例 Listing，方便前端联调。
 */
@Component
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            UserRepository userRepository,
            ListingRepository listingRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void init() {
        User admin = userRepository
                .findByEmail("admin@admin.com")
                .orElseGet(() -> userRepository.save(new User(
                        "admin@admin.com",
                        passwordEncoder.encode("12345678"),
                        "Admin Rider",
                        "London",
                        "热爱单板的管理员", 
                        4.9,
                        32)));

        if (listingRepository.count() == 0) {
            listingRepository.save(new Listing(
                    "Burton Custom X",
                    "轻度使用，附送固定器",
                    ListingCondition.LIKE_NEW,
                    BigDecimal.valueOf(450.0),
                    "London",
                    TradeOption.FACE_TO_FACE,
                    false,
                    "https://images.example.com/boards/custom-x.jpg",
                    admin));

            listingRepository.save(new Listing(
                    "Jones Mountain Twin",
                    "保养良好，含原装滑雪包",
                    ListingCondition.GOOD,
                    BigDecimal.valueOf(380.0),
                    "Innsbruck",
                    TradeOption.COURIER,
                    false,
                    "https://images.example.com/boards/mountain-twin.jpg",
                    admin));
        }
    }
}
