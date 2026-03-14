package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.entity.RepairOrder;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.RepairOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderControllerTest {

    @InjectMocks
    private RepairOrderController repairOrderController;

    @Mock
    private RepairOrderService repairOrderService;

    @Test
    void createRepairOrderShouldAlwaysBindCurrentUser() {
        MockHttpServletRequest request = buildRequest(2, 123L);
        RepairOrder order = new RepairOrder();
        order.setUserId(999L);
        when(repairOrderService.createRepairOrder(any(RepairOrder.class))).thenReturn(true);

        repairOrderController.createRepairOrder(order, request);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderService).createRepairOrder(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(123L, captor.getValue().getUserId());
    }

    @Test
    void feedbackShouldRejectOtherUsersOrder() {
        MockHttpServletRequest request = buildRequest(2, 10L);
        RepairOrder order = new RepairOrder();
        order.setOrderId(5L);
        order.setUserId(11L);
        when(repairOrderService.getById(5L)).thenReturn(order);
        Map<String, Object> payload = new HashMap<>();
        payload.put("feedback", "ok");
        payload.put("rating", 5);

        assertThrows(BusinessException.class, () -> repairOrderController.feedbackOrder(5L, payload, request));
    }

    @Test
    void getRepairOrderPageShouldForceCurrentUserForNormalUser() {
        MockHttpServletRequest request = buildRequest(2, 55L);
        doReturn(new Page<RepairOrder>())
                .when(repairOrderService)
                .getRepairOrderPage(eq(1), eq(10), eq(55L), isNull(), isNull(), isNull(), isNull());

        repairOrderController.getRepairOrderPage(1, 10, 999L, null, null, null, null, request);

        verify(repairOrderService).getRepairOrderPage(1, 10, 55L, null, null, null, null);
    }

    private MockHttpServletRequest buildRequest(int userType, long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userType", userType);
        request.setAttribute("userId", userId);
        return request;
    }
}
