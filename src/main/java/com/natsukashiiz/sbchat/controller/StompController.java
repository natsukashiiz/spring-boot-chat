package com.natsukashiiz.sbchat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class StompController {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String send(String message, Principal principal) {
        System.out.println("Send>>> " + message+ " from " + principal.getName());
        return message;
    }
}
