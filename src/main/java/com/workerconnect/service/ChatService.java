package com.workerconnect.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.workerconnect.dto.ChatMessageDto;
import com.workerconnect.model.ChatMessage;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.ChatRepository;
import com.workerconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository repository;

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    public ChatMessage save(ChatMessageDto dto){

        ChatMessage message = new ChatMessage();

        message.setBooking(
                bookingRepository.findById(dto.getBookingId()).get());

        message.setSender(
                userRepository.findById(dto.getSenderId()).get());

        message.setReceiver(
                userRepository.findById(dto.getReceiverId()).get());

        message.setMessage(dto.getMessage());

        message.setSentTime(LocalDateTime.now());

        message.setSeen(false);

        System.out.println("***************************************");
        System.out.println("Booking: " + dto.getBookingId());
        System.out.println("Sender: " + dto.getSenderId());
        System.out.println("Receiver: " + dto.getReceiverId());
        System.out.println("Message: " + dto.getMessage());
        System.out.println("*****************************");

        return repository.save(message);

    }

}
