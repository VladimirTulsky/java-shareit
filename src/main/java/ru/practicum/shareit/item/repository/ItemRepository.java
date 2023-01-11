package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PostItemDto;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<ItemDto> findAll(long userId);

    Optional<ItemDto> findItem(long itemId);

    Optional<ItemDto> findItemForUpdate(long userId, long itemId);

    List<ItemDto> searchItem(String text);

    ItemDto create(long userId, PostItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);
}
