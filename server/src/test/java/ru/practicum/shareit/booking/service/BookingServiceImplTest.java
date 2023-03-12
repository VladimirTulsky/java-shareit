package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.strategy.searchByBooker.*;
import ru.practicum.shareit.booking.strategy.searchByOwner.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    UserRepository userRepository = Mockito.mock(UserRepository.class);
    BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    ItemRepository itemRepository = Mockito.mock(ItemRepository.class);

    List<BookingSearchByBooker> bookingSearchesByBooker = List.of(
            new AllBookingSearchByBooker(bookingRepository),
            new CurrentBookingSearchByBooker(bookingRepository),
            new PastBookingSearchByBooker(bookingRepository),
            new FutureBookingSearchByBooker(bookingRepository),
            new WaitingBookingSearchByBooker(bookingRepository),
            new RejectedBookingSearchByBooker(bookingRepository));

    List<BookingSearchByOwner> bookingSearchesByOwner = List.of(
            new AllBookingSearchByOwner(bookingRepository),
            new CurrentBookingSearchByOwner(bookingRepository),
            new PastBookingSearchByOwner(bookingRepository),
            new FutureBookingSearchByOwner(bookingRepository),
            new WaitingBookingSearchByOwner(bookingRepository),
            new RejectedBookingSearchByOwner(bookingRepository));

    BookingServiceImpl bookingServiceImpl = new BookingServiceImpl(userRepository, bookingRepository, itemRepository,
            bookingSearchesByBooker, bookingSearchesByOwner);

    BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
            1L,
            2L,
            null);
    User user = new User(
            1L,
            "name",
            "email@email.ru");
    User user2 = new User(
            2L,
            "name",
            "email@email.ru");
    Item item = new Item(
            1L,
            "name",
            "description",
            true,
            user,
            null);

    @Test
    void create_whenAllIsOk_thenBookingSaved() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        BookingDto bDto = BookingMapper.toBookingDto(booking);
        BookingDtoResponse forCheck = BookingMapper.toBookingDtoResponse(booking);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user2));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoResponse actual = bookingServiceImpl.create(2L, bookingDto);
        assertEquals(forCheck, actual);
        assertEquals(bDto.getId(), bookingDto.getId());
        verify(bookingRepository).save(any());
    }

    @Test
    void create_whenItemNotFound_thenExceptionThrown() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.create(1L, bookingDto));
        assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void create_whenUserNotFound_thenExceptionThrown() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.create(1L, bookingDto));
        assertEquals("Wrong user", ex.getMessage());
    }

    @Test
    void create_whenOwnerTryingToBookHisItem_thenExceptionThrown() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user2));

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.create(1L, bookingDto));
        assertEquals("You can't book your item", ex.getMessage());
    }

    @Test
    void create_whenItemNotAvailable_thenExceptionThrown() {
        Item itemTest = item;
        itemTest.setId(49L);
        itemTest.setAvailable(false);
        bookingDto.setItemId(49L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingServiceImpl.create(4L, bookingDto));
        assertEquals("Item not available now for booking", ex.getMessage());
    }

    @Test
    void changeStatus_whenItemOwnerChangeToApprove_thenStatusUpdated() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoResponse resp = bookingServiceImpl.changeStatus(user.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, resp.getStatus());
        verify(bookingRepository).save(any());
    }

    @Test
    void changeStatus_whenItemOwnerChangeToReject_thenStatusUpdated() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoResponse resp = bookingServiceImpl.changeStatus(user.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, resp.getStatus());
        verify(bookingRepository).save(any());
    }

    @Test
    void changeStatus_whenBookingNotFound_thenExceptionThrown() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.changeStatus(1L, 1L, true));
        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void changeStatus_whenNotItemOwner_thenExceptionThrown() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.changeStatus(999L, 1L, true));
        assertEquals("You can't confirm this booking", ex.getMessage());
    }

    @Test
    void changeStatus_whenBookingAlreadyApproved_thenExceptionThrown() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingServiceImpl.changeStatus(1L, 1L, true));
        assertEquals("You can't change status after approving", ex.getMessage());
    }

    @Test
    void getBookingInfo_whenOwner_thenReturnInfo() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDtoResponse resp = bookingServiceImpl.getBookingInfo(user.getId(), booking.getId());
        assertNotNull(resp);
        assertEquals(booking.getItem().getName(), resp.getItem().getName());
    }

    @Test
    void getBookingInfo_whenNotOwner_thenReturnInfo() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.getBookingInfo(999L, booking.getId()));
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void getBookingInfo_whenBookingNotFound_thenExceptionThrown() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.getBookingInfo(1L, 1L));
        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void getByBooker_whenBookerAllState_thenReturnBooking() {

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        PageRequest p = PageRequest.of(0, 20);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(Collections.singletonList(booking));

        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "ALL", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getByBooker_whenBookerCurrentState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByBookerCurrent(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "CURRENT", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByBookerCurrent(anyLong(), any(), any());
    }

    @Test
    void getByBooker_whenBookerPastState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByBookerPast(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "PAST", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByBookerPast(anyLong(), any(), any());
    }

    @Test
    void getByBooker_whenBookerFutureState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByBookerFuture(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "FUTURE", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByBookerFuture(anyLong(), any(), any());
    }

    @Test
    void getByBooker_whenBookerWaitingStatus_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByBookerAndStatus(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "WAITING", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByBookerAndStatus(anyLong(), any(), any());
    }

    @Test
    void getByBooker_whenBookerRejectedStatus_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByBookerAndStatus(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByBooker(user.getId(), "REJECTED", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByBookerAndStatus(anyLong(), any(), any());
    }

    @Test
    void getByOwner_whenBookerAllState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "ALL", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerIdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getByOwner_whenBookerCurrentState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerCurrent(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "CURRENT", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerCurrent(anyLong(), any(), any());
    }

    @Test
    void getByOwner_whenBookerPastState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerPast(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "PAST", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerPast(anyLong(), any(), any());
    }

    @Test
    void getByOwner_whenBookerFutureState_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerFuture(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "FUTURE", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerFuture(anyLong(), any(), any());
    }

    @Test
    void getByOwner_whenBookerWaitingStatus_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerAndStatus(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "WAITING", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerAndStatus(anyLong(), any(), any());
    }

    @Test
    void getByOwner_whenBookerRejectedStatus_thenReturnBooking() {
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(bookingRepository.findByItemOwnerAndStatus(anyLong(), any(), any())).thenReturn(Collections.singletonList(booking));

        PageRequest p = PageRequest.of(0, 20);
        List<BookingDtoResponse> resp = bookingServiceImpl.getByOwner(user.getId(), "REJECTED", p);
        assertFalse(resp.isEmpty());
        assertEquals(booking.getItem().getName(), resp.get(0).getItem().getName());
        verify(bookingRepository, times(1)).findByItemOwnerAndStatus(anyLong(), any(), any());
    }

    @Test
    void getByBooker_whenBookerNotFound_thenExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        PageRequest p = PageRequest.of(0, 20);
        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.getByBooker(user.getId(), "ALL", p));
        assertEquals("Booker not found", ex.getMessage());
    }

    @Test
    void getByOwner_whenBookerNotFound_thenExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        PageRequest p = PageRequest.of(0, 20);
        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> bookingServiceImpl.getByOwner(user.getId(), "ALL", p));
        assertEquals("Owner not found", ex.getMessage());
    }
}