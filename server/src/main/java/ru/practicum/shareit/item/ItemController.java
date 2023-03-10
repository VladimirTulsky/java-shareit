package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoBooking> findAll(@RequestHeader("X-Sharer-User-Id") long id,
                                        @RequestParam(defaultValue = "0", required = false) int from,
                                        @RequestParam(defaultValue = "20", required = false) int size) {
        PageRequest p = PageRequest.of(from / size, size);
        return itemService.findAll(id, p);
    }

    @GetMapping("/{itemId}")
    public ItemDtoBooking findItem(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        return itemService.findItem(userId, itemId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text,
                                    @RequestParam(defaultValue = "0", required = false) int from,
                                    @RequestParam(defaultValue = "20", required = false) int size) {
        PageRequest p = PageRequest.of(from / size, size);
        return itemService.searchItem(text, p);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}