package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PostItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> findAll(long userId) {
        log.info("Items sent");
        return itemRepository.findAll(userId);
    }

    @Override
    public ItemDto findItem(long itemId) {
        log.info("Item sent");
        return itemRepository.findItem(itemId).orElseThrow(() -> {
            log.warn("Item not found");
            throw new ObjectNotFoundException("Item not found");
        });
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("Search results sent");
        if (text.isBlank()) return Collections.emptyList();
        return itemRepository.searchItem(text);
    }

    @Override
    public ItemDto create(long userId, PostItemDto itemDto) {
        userRepository.getById(userId).orElseThrow(() -> {
            log.warn("User not found");
            throw new ObjectNotFoundException("User not found");
        });
        log.info("Item created");
        return itemRepository.create(userId, itemDto);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        itemRepository.findItemForUpdate(userId, itemId).orElseThrow(() -> {
            log.warn("Item not found for update");
            throw new ObjectNotFoundException("Item not found for update");
        });
        log.info("Item updated");
        return itemRepository.update(userId, itemId, itemDto);
    }
}
