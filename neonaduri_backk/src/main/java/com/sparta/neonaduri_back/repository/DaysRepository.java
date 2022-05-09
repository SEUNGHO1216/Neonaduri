package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Days;
import com.sparta.neonaduri_back.model.Places;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DaysRepository extends JpaRepository<Days, Long> {

//    List<Days> findByPlacesIn(Collection<List<Places>> places);
}
