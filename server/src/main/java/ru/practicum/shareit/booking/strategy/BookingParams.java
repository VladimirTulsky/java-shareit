package ru.practicum.shareit.booking.strategy;

import lombok.*;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@AllArgsConstructor
public class BookingParams {
    private final Long userId;
    private final Pageable p;
}
