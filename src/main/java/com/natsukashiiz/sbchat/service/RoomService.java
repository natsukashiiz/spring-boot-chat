package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.repository.RoomMemberRepository;
import com.natsukashiiz.sbchat.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class RoomService {

    private final AuthService authService;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

}
