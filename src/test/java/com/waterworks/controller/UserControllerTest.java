package com.waterworks.controller;

import com.waterworks.entity.User;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Test
    void changePasswordShouldUseUserIdFromToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 100L);
        request.setAttribute("userType", 2);

        Map<String, String> payload = new HashMap<>();
        payload.put("userId", "999");
        payload.put("oldPassword", "oldPwd");
        payload.put("newPassword", "newPwd");

        when(userService.changePassword(eq(100L), anyString(), anyString())).thenReturn(true);

        userController.changePassword(payload, request);

        verify(userService).changePassword(100L, "oldPwd", "newPwd");
    }

    @Test
    void getUserByIdShouldRejectNonAdminAccessToOthers() {
        HttpServletRequest request = buildRequest(2, 10L);
        assertThrows(BusinessException.class, () -> userController.getUserById(11L, request));
    }

    @Test
    void getUserByIdShouldSanitizeSensitiveFields() {
        HttpServletRequest request = buildRequest(1, 1L);
        User user = new User();
        user.setUserId(2L);
        user.setPassword("hashed");
        user.setSalt("salt");
        when(userService.getById(2L)).thenReturn(user);

        User data = userController.getUserById(2L, request).getData();

        assertEquals(2L, data.getUserId());
        assertNull(data.getPassword());
        assertNull(data.getSalt());
    }

    private HttpServletRequest buildRequest(int userType, long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userType", userType);
        request.setAttribute("userId", userId);
        return request;
    }
}
