package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    long id;
    @NotBlank
    String name;
    @Size(min = 1, max = 200, message = "Wrong text length")
    String description;
    @NotNull
    Boolean available;
    long owner;
}
