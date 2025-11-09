package com.codex.backend.repository;

import com.codex.backend.domain.listing.Listing;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Listing 持久层接口，负责雪板发布的增删改查。
 */
public interface ListingRepository extends JpaRepository<Listing, UUID> {
}
