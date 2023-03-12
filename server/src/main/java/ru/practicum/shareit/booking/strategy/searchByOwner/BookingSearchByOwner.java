package ru.practicum.shareit.booking.strategy.searchByOwner;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.BookingParams;

import java.util.List;

public interface BookingSearchByOwner {
    List<Booking> search(BookingParams params);

    String getType();
}
