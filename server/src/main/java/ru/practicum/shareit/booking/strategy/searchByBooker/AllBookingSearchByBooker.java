package ru.practicum.shareit.booking.strategy.searchByBooker;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingParams;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AllBookingSearchByBooker implements BookingSearchByBooker {
    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> search(BookingParams params) {
        return bookingRepository.findAllByBookerIdOrderByStartDesc(params.getUserId(), params.getP());
    }

    @Override
    public String getType() {
        return "ALL";
    }
}
