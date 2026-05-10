package com.reservations.hotel.business.service;

import com.reservations.hotel.business.domain.CreateGuestRequest;
import com.reservations.hotel.business.domain.GuestResponse;
import com.reservations.hotel.data.entity.Guest;
import com.reservations.hotel.data.repository.GuestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional(readOnly = true)
    public GuestResponse getById(long id) {
        return guestRepository.findById(id)
            .map(GuestResponse::from)
            .orElseThrow(() -> new EntityNotFoundException("Guest not found"));
    }

    @Transactional(readOnly = true)
    public List<GuestResponse> findByEmail(String email) {
        return guestRepository.findByEmailAddress(email).stream()
            .map(GuestResponse::from)
            .toList();
    }

    @Transactional
    public GuestResponse createGuest(CreateGuestRequest request) {
        Guest guest = new Guest();
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setEmailAddress(request.getEmailAddress());
        guest.setAddress(request.getAddress());
        guest.setCountry(request.getCountry());
        guest.setState(request.getState());
        guest.setPhoneNumber(request.getPhoneNumber());
        return GuestResponse.from(guestRepository.save(guest));
    }
}
