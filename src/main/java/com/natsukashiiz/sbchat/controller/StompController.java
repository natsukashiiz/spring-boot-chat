package com.natsukashiiz.sbchat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String send(String message) {
        System.out.println("Send>>> " + message);
        return message;
    }
}
