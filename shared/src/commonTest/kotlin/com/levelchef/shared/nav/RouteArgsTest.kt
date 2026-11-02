package com.levelchef.shared.nav

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteArgsTest {

    @Test
    fun uuid_ids_pass_through_unchanged() {
        assertEquals("123e4567-e89b-42d3-a456-426614174000", encodeRouteArg("123e4567-e89b-42d3-a456-426614174000"))
    }

    @Test
    fun route_breaking_characters_are_percent_encoded() {
        assertEquals("a%2Fb%3Fc%3Dd%23e%25f", encodeRouteArg("a/b?c=d#e%f"))
        assertEquals("chili%20%26%20lime", encodeRouteArg("chili & lime"))
    }

    @Test
    fun non_ascii_is_encoded_as_utf8_bytes() {
        assertEquals("p%C3%B6rk%C3%B6lt", encodeRouteArg("pörkölt"))
    }
}
