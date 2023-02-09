package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    private Long requestor_id;
    @NotBlank(groups = Create.class)
    @Size(groups = Create.class, min = 1, max = 200)
    private String description;
    private LocalDateTime created;
}
