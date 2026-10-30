package com.transporttn.backend.repository;

import com.transporttn.backend.entity.Reservation;
import com.transporttn.backend.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    @Query("SELECT COALESCE(SUM(rs.seatsCount), 0) FROM ReservationSeat rs " +
           "WHERE rs.ligne.id = :ligneId AND rs.travelDate = :travelDate " +
           "AND rs.reservation.status <> :excludedStatus")
    Integer countReservedSeats(@Param("ligneId") Long ligneId,
                                @Param("travelDate") LocalDate travelDate,
                                @Param("excludedStatus") Reservation.Status excludedStatus);
}
