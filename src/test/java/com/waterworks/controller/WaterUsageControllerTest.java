package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.entity.WaterUsage;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.WaterUsageService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterUsageControllerTest {

    @InjectMocks
    private WaterUsageController waterUsageController;

    @Mock
    private WaterUsageService waterUsageService;

    @Test
    void getUsagePageShouldForceCurrentUserForNormalUser() {
        MockHttpServletRequest request = buildRequest(2, 88L);
        doReturn(new Page<WaterUsage>())
                .when(waterUsageService)
                .getUsagePage(eq(1), eq(10), eq(88L), isNull(), isNull(), isNull());

        waterUsageController.getUsagePage(1, 10, 999L, null, null, null, request);

        verify(waterUsageService).getUsagePage(1, 10, 88L, null, null, null);
    }

    @Test
    void getUsageByIdShouldRejectOtherUsersDataForNormalUser() {
        MockHttpServletRequest request = buildRequest(2, 1L);
        WaterUsage usage = new WaterUsage();
        usage.setUsageId(7L);
        usage.setUserId(2L);
        when(waterUsageService.getById(7L)).thenReturn(usage);

        assertThrows(BusinessException.class, () -> waterUsageController.getUsageById(7L, request));
    }

    private MockHttpServletRequest buildRequest(int userType, long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userType", userType);
        request.setAttribute("userId", userId);
        return request;
    }
}
