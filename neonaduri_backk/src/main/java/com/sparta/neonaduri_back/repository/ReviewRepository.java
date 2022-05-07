package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
