package com.reservations.landon.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "RESERVATION_NIGHT",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_RES_NIGHT_ROOM_DATE",
        columnNames = {"ROOM_ID", "STAY_DATE"}
    )
)
public class ReservationNight {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RESERVATION_NIGHT_ID")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID", nullable = false)
    private Room room;

    @Column(name = "STAY_DATE", nullable = false)
    private LocalDate stayDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getStayDate() {
        return stayDate;
    }

    public void setStayDate(LocalDate stayDate) {
        this.stayDate = stayDate;
    }
}
