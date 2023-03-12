package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.BookingParams;
import ru.practicum.shareit.booking.strategy.searchByBooker.BookingSearchByBooker;
import ru.practicum.shareit.booking.strategy.searchByOwner.BookingSearchByOwner;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final Map<String, BookingSearchByBooker> bookingSearchByBookerMap = new HashMap<>();
    private final Map<String, BookingSearchByOwner> bookingSearchByOwnerMap = new HashMap<>();

    @Autowired
    public BookingServiceImpl(UserRepository userRepository,
                              BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              List<BookingSearchByBooker> bookingSearchesByBooker,
                              List<BookingSearchByOwner> bookingSearchesByOwner) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        bookingSearchesByBooker.forEach(bookingSearch -> bookingSearchByBookerMap.put(bookingSearch.getType(), bookingSearch));
        bookingSearchesByOwner.forEach(bookingSearch -> bookingSearchByOwnerMap.put(bookingSearch.getType(), bookingSearch));
    }

    @Override
    @Transactional
    public BookingDtoResponse create(long bookerId, BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> {
            throw new ObjectNotFoundException("Item not found");
        });
        User user = userRepository.findById(bookerId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Wrong user");
        });
        if (item.getOwner().getId() == bookerId) throw new ObjectNotFoundException("You can't book your item");
        if (!item.getAvailable()) throw new BadRequestException("Item not available now for booking");
        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = bookingRepository.save(BookingMapper.toBooking(bookingDto, item, user));
        BookingDtoResponse bookingDtoResponse = BookingMapper.toBookingDtoResponse(booking);
        log.info("Item created");
        return bookingDtoResponse;
    }

    @Override
    @Transactional
    public BookingDtoResponse changeStatus(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Booking not found");
        });
        Item item = booking.getItem();
        if (userId != item.getOwner().getId()) throw new ObjectNotFoundException("You can't confirm this booking");
        if (booking.getStatus() == BookingStatus.APPROVED)
            throw new BadRequestException("You can't change status after approving");
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else booking.setStatus(BookingStatus.REJECTED);
        return BookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoResponse getBookingInfo(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Booking not found");
        });
        Item item = booking.getItem();
        if (booking.getBooker().getId() == userId || item.getOwner().getId() == userId) {
            return BookingMapper.toBookingDtoResponse(booking);
        } else throw new ObjectNotFoundException("Access denied");
    }

    @Override
    public List<BookingDtoResponse> getByBooker(long userId, String state, Pageable p) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Booker not found");
        });
        List<Booking> books = bookingSearchByBookerMap.get(state).search(new BookingParams(userId, p));
        return books.stream()
                .map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoResponse> getByOwner(long userId, String state, Pageable p) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Owner not found");
        });
        List<Booking> books = bookingSearchByOwnerMap.get(state).search(new BookingParams(userId, p));
        return books.stream()
                .map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }
}