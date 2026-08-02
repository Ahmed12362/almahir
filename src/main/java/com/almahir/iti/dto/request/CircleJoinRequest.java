package com.almahir.iti.dto.request;

public record CircleJoinRequest(
        String password // required only when the circle is PRIVATE
) {
}