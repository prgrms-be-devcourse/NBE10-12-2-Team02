package com.back.domain.schedule.repository;

import com.back.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // concertId로 첫 번째 Schedule 조회
    Optional<Schedule> findFirstByConcertConcertId(Long concertId);

    List<Schedule> findByConcertConcertId(Long concertId);
}