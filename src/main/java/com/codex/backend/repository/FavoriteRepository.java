package com.codex.backend.repository;

import com.codex.backend.domain.favorite.Favorite;
import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 收藏仓储：查询与增删用户收藏关系。
 */
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserAndArchivedFalseOrderByCreatedAtDesc(User user);

    Optional<Favorite> findByUserAndListing(User user, Listing listing);
}
