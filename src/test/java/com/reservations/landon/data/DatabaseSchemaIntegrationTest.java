package com.reservations.landon.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DatabaseSchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void schemaLoadsSeedData() {
        Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ROOM", Integer.class);
        Integer guestCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM GUEST", Integer.class);
        Integer reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM RESERVATION", Integer.class);

        assertThat(roomCount).isEqualTo(28);
        assertThat(guestCount).isEqualTo(200);
        assertThat(reservationCount).isEqualTo(1);
    }

    @Test
    void seededReservationHasExpectedRoomGuestStatusAndPrice() {
        String roomNumber = jdbcTemplate.queryForObject("""
            SELECT rm.ROOM_NUMBER
            FROM RESERVATION r
            JOIN ROOM rm ON rm.ROOM_ID = r.ROOM_ID
            """, String.class);
        String guestName = jdbcTemplate.queryForObject("""
            SELECT g.FIRST_NAME || ' ' || g.LAST_NAME
            FROM RESERVATION r
            JOIN GUEST g ON g.GUEST_ID = r.GUEST_ID
            """, String.class);
        String status = jdbcTemplate.queryForObject("SELECT STATUS FROM RESERVATION", String.class);
        BigDecimal totalPrice = jdbcTemplate.queryForObject("SELECT TOTAL_PRICE FROM RESERVATION", BigDecimal.class);

        assertThat(roomNumber).isEqualTo("C2");
        assertThat(guestName).isEqualTo("Judith Young");
        assertThat(status).isEqualTo("CONFIRMED");
        assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("218.00"));
    }

    @Test
    void reservationDateRangeConstraintRejectsInvalidRange() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO RESERVATION (ROOM_ID, GUEST_ID, CHECK_IN_DATE, CHECK_OUT_DATE, STATUS, TOTAL_PRICE)
            VALUES (1, 1, '2026-05-10', '2026-05-10', 'PENDING', 100.00)
            """))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reservationStatusConstraintRejectsUnknownStatus() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO RESERVATION (ROOM_ID, GUEST_ID, CHECK_IN_DATE, CHECK_OUT_DATE, STATUS, TOTAL_PRICE)
            VALUES (1, 1, '2026-05-10', '2026-05-11', 'HELD', 100.00)
            """))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
