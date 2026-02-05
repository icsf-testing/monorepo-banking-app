Here's a JUnit 5 test class for the WebConfig class:

```java
package com.banking.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebConfigTest {

    private WebConfig webConfig;
    private ResourceHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
        registry = mock(ResourceHandlerRegistry.class);
    }

    @Test
    void testAddResourceHandlers() {
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);

        webConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/css/**");
        verify(registry).addResourceHandler("/js/**");
        verify(registry).addResourceHandler("/images/**");
        verify(registry).addResourceHandler("/fonts/**");

        verify(registration, times(4)).addResourceLocations(anyString());
    }

    @Test
    void testSecurityFilterBean() {
        FilterRegistrationBean<Filter> bean = webConfig.securityFilter();

        assertNotNull(bean);
        assertTrue(bean.getFilter() instanceof SecurityFilter);
        assertEquals(1, bean.getUrlPatterns().size());
        assertTrue(bean.getUrlPatterns().contains("/*"));
    }

    @Test
    void testSecurityFilterDoFilterForRestrictedResource() throws IOException, ServletException {
        SecurityFilter filter = new SecurityFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.setRequestURI("/admin/dashboard");

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertEquals("Access Denied", response.getErrorMessage());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void testSecurityFilterDoFilterForNonRestrictedResource() throws IOException, ServletException {
        SecurityFilter filter = new SecurityFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.setRequestURI("/public/page");

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }

    @Test
    void testIsRestrictedResource() {
        SecurityFilter filter = new SecurityFilter();

        assertTrue(filter.isRestrictedResource("/admin/users"));
        assertTrue(filter.isRestrictedResource("/config/app.conf"));
        assertTrue(filter.isRestrictedResource("/logs/error.log"));
        assertFalse(filter.isRestrictedResource("/public/index.html"));
        assertFalse(filter.isRestrictedResource("/api/users"));
    }

    @Test
    void testIsAuthenticated() {
        SecurityFilter filter = new SecurityFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertFalse(filter.isAuthenticated(request));
    }

    @Test
    void testSecurityFilterInitAndDestroy() throws ServletException {
        SecurityFilter filter = new SecurityFilter();

        // These methods are empty, so we just call them for coverage
        filter.init(null);
        filter.destroy();
    }
}
```
