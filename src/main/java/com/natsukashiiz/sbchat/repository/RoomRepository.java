package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}