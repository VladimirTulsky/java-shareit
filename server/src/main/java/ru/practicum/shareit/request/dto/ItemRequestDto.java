package ru.practicum.shareit.request.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ItemRequestDto {
    private Long id;
    private Long requestorId;
    private String description;
    private LocalDateTime created;
}
