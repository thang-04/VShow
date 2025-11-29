package com.vticket.identity.app.usercase;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * @param key  tên key trong file messages_vi.properties
     * @param args tham số truyền vào chuỗi message
     * Locale default đang set là "vi-VN"
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, new Locale("vi", "VN"));
    }
}
