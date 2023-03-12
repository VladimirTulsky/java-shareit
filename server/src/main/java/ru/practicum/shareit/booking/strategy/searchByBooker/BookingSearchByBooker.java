package ru.practicum.shareit.booking.strategy.searchByBooker;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.BookingParams;

import java.util.List;

public interface BookingSearchByBooker {
    List<Booking> search(BookingParams params);

    String getType();
}
