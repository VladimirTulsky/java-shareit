package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> findAll(long userId) {
        log.info("Items sent");
        return itemRepository.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findItem(long itemId) {
        log.info("Item sent");
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item not found");
            throw new ObjectNotFoundException("Item not found");
        });
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found");
            throw new ObjectNotFoundException("User not found");
        });
        log.info("Item created");
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, userId));
        itemDto.setId(item.getId());
        return itemDto;
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item not found for update");
            throw new ObjectNotFoundException("Item not found for update");
        });
        if (item.getOwner().equals(userId)) {
            if (itemDto.getName() != null) item.setName(itemDto.getName());
            if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
            if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
            itemRepository.save(item);
            log.info("Item updated");
        } else {
            log.warn("Item not found for update");
            throw new ObjectNotFoundException("Item not found for update");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("Search results sent");
        if (text.isBlank()) return Collections.emptyList();
        return itemRepository.findByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(text, text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
