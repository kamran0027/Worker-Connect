package com.workerconnect.controller;

import com.workerconnect.dto.BookingDto;
import com.workerconnect.model.Worker;
import com.workerconnect.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private WorkerService workerService;
    @Mock
    private BookingService bookingService;
    @Mock
    private AgreementService agreementService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @Test
    void bookWorkerPageShouldPopulateBookingDtoWithWorkerId() {
        Worker worker = new Worker();
        worker.setId(7L);
        worker.setFullName("Test Worker");
        when(workerService.findById(7L)).thenReturn(worker);

        ExtendedModelMap model = new ExtendedModelMap();
        String viewName = userController.bookWorkerPage(7L, model);

        assertEquals("user/booking/create", viewName);
        BookingDto bookingDto = (BookingDto) model.get("bookingDto");
        assertNotNull(bookingDto);
        assertEquals(7L, bookingDto.getWorkerId());
    }
}
