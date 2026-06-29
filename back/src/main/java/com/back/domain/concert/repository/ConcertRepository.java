package com.back.domain.concert.repository;

import com.back.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {

    // keyword 검색 (없으면 전체)
    @Query("SELECT c FROM Concert c WHERE (:keyword IS NULL OR c.concertName LIKE %:keyword%)")
    List<Concert> findByKeyword(@Param("keyword") String keyword);
}