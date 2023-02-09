package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Create;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDtoResponse {
    private Long id;
    private Long requestorId;
    @Size(groups = Create.class, min = 1, max = 200)
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}