package com.codex.backend.config;

import com.codex.backend.domain.favorite.Favorite;
import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.listing.ListingCondition;
import com.codex.backend.domain.listing.TradeOption;
import com.codex.backend.domain.message.Message;
import com.codex.backend.domain.message.MessageThread;
import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.trip.TripJoinRequest;
import com.codex.backend.domain.trip.TripMessage;
import com.codex.backend.domain.trip.TripParticipant;
import com.codex.backend.domain.trip.TripParticipantRole;
import com.codex.backend.domain.trip.TripRequestStatus;
import com.codex.backend.domain.trip.TripStatus;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.FavoriteRepository;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.repository.MessageRepository;
import com.codex.backend.repository.MessageThreadRepository;
import com.codex.backend.repository.TripJoinRequestRepository;
import com.codex.backend.repository.TripMessageRepository;
import com.codex.backend.repository.TripParticipantRepository;
import com.codex.backend.repository.TripRepository;
import com.codex.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final FavoriteRepository favoriteRepository;
    private final MessageThreadRepository messageThreadRepository;
    private final MessageRepository messageRepository;
    private final TripRepository tripRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final TripJoinRequestRepository tripJoinRequestRepository;
    private final TripMessageRepository tripMessageRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            UserRepository userRepository,
            ListingRepository listingRepository,
            FavoriteRepository favoriteRepository,
            MessageThreadRepository messageThreadRepository,
            MessageRepository messageRepository,
            TripRepository tripRepository,
            TripParticipantRepository tripParticipantRepository,
            TripJoinRequestRepository tripJoinRequestRepository,
            TripMessageRepository tripMessageRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.messageThreadRepository = messageThreadRepository;
        this.messageRepository = messageRepository;
        this.tripRepository = tripRepository;
        this.tripParticipantRepository = tripParticipantRepository;
        this.tripJoinRequestRepository = tripJoinRequestRepository;
        this.tripMessageRepository = tripMessageRepository;
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

        User buyer = userRepository
                .findByEmail("buyer@codex.com")
                .orElseGet(() -> userRepository.save(new User(
                        "buyer@codex.com",
                        passwordEncoder.encode("12345678"),
                        "Mountain Lover",
                        "Zurich",
                        "收藏滑雪板达人",
                        4.6,
                        18)));

        User traveler = userRepository
                .findByEmail("traveler@codex.com")
                .orElseGet(() -> userRepository.save(new User(
                        "traveler@codex.com",
                        passwordEncoder.encode("12345678"),
                        "Alps Rookie",
                        "Munich",
                        "刚入坑的雪友，期待结伴",
                        4.2,
                        5)));

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

        if (favoriteRepository.count() == 0) {
            listingRepository
                    .findAll()
                    .stream()
                    .findFirst()
                    .ifPresent(listing -> favoriteRepository.save(new Favorite(buyer, listing)));
        }

        if (messageThreadRepository.count() == 0) {
            listingRepository
                    .findAll()
                    .stream()
                    .findFirst()
                    .ifPresent(listing -> {
                        MessageThread thread = messageThreadRepository.save(new MessageThread(
                                listing,
                                listing.getSeller(),
                                buyer,
                                listing.getTitle()));
                        messageRepository.save(new Message(thread, buyer, "你好，这块板子还在吗？"));
                        messageRepository.save(new Message(thread, listing.getSeller(), "在的，刚刚调试过边刃。"));
                    });
        }

        if (tripRepository.count() == 0) {
            Instant start = Instant.now().plus(3, ChronoUnit.DAYS);
            Instant end = start.plus(2, ChronoUnit.DAYS);
            Trip trip = tripRepository.save(new Trip(
                    "阿尔卑斯周末团",
                    "Zermatt",
                    "轻松两日行程，适合中级及以上滑手",
                    start,
                    end,
                    TripStatus.UPCOMING,
                    admin));

            TripParticipant organizerParticipant = tripParticipantRepository.save(
                    new TripParticipant(trip, admin, TripParticipantRole.ORGANIZER));
            trip.getParticipants().add(organizerParticipant);

            TripParticipant member = tripParticipantRepository.save(
                    new TripParticipant(trip, buyer, TripParticipantRole.MEMBER));
            trip.getParticipants().add(member);

            TripJoinRequest joinRequest = tripJoinRequestRepository.save(
                    new TripJoinRequest(trip, traveler, TripRequestStatus.PENDING, "期待加入一起出发"));
            trip.getJoinRequests().add(joinRequest);

            TripMessage welcome = tripMessageRepository.save(new TripMessage(trip, admin, "欢迎加入，本周五集合！"));
            trip.getMessages().add(welcome);
            TripMessage reply = tripMessageRepository.save(new TripMessage(trip, buyer, "收到，已经准备好装备！"));
            trip.getMessages().add(reply);
        }
    }
}
