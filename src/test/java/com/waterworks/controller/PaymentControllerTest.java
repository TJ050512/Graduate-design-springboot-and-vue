package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.entity.Payment;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    @Test
    void getPaymentPageShouldForceCurrentUserForNormalUser() {
        MockHttpServletRequest request = buildRequest(2, 101L);
        doReturn(new Page<Payment>())
                .when(paymentService)
                .getPaymentPage(eq(1), eq(10), eq(101L), isNull(), isNull(), isNull());

        paymentController.getPaymentPage(1, 10, 999L, null, null, null, request);

        verify(paymentService).getPaymentPage(1, 10, 101L, null, null, null);
    }

    @Test
    void payShouldRejectNonOwnerOperation() {
        MockHttpServletRequest request = buildRequest(2, 1L);
        Payment payment = new Payment();
        payment.setPaymentId(9L);
        payment.setUserId(2L);
        when(paymentService.getById(9L)).thenReturn(payment);

        assertThrows(BusinessException.class, () -> paymentController.pay(9L, 2, request));
        verify(paymentService, never()).pay(eq(9L), eq(2));
    }

    @Test
    void payShouldAllowOwnerOperation() {
        MockHttpServletRequest request = buildRequest(2, 2L);
        Payment payment = new Payment();
        payment.setPaymentId(9L);
        payment.setUserId(2L);
        when(paymentService.getById(9L)).thenReturn(payment);
        when(paymentService.pay(9L, 2)).thenReturn(true);

        paymentController.pay(9L, 2, request);

        verify(paymentService).pay(9L, 2);
    }

    private MockHttpServletRequest buildRequest(int userType, long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userType", userType);
        request.setAttribute("userId", userId);
        return request;
    }
}
