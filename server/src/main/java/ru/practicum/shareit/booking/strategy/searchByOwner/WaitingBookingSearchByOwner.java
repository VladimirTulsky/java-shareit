package ru.practicum.shareit.booking.strategy.searchByOwner;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingParams;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WaitingBookingSearchByOwner implements BookingSearchByOwner {
    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> search(BookingParams params) {
        return bookingRepository.findByItemOwnerAndStatus(params.getUserId(), BookingStatus.WAITING, params.getP());
    }

    @Override
    public String getType() {
        return "WAITING";
    }
}