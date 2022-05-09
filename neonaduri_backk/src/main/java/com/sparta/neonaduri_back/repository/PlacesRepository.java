package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Places;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacesRepository extends JpaRepository<Places, Long> {

//    List<Places> findByPlaceNameContaining(String placeName);
}
