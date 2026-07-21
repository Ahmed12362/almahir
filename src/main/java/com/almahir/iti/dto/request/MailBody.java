package com.almahir.iti.dto.request;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {
}
