package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PostItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAll(long userId);

    ItemDto findItem(long itemId);

    List<ItemDto> searchItem(String text);

    ItemDto create(long userId, PostItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);
}