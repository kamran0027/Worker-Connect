package com.workerconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workerconnect.model.ChatMessage;

public interface ChatRepository extends JpaRepository<ChatMessage,Long>{

    List<ChatMessage> findByBookingIdOrderBySentTimeAsc(Long bookingId);

}
