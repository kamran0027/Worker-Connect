package com.workerconnect.controller.chat;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.workerconnect.dto.ChatMessageDto;
import com.workerconnect.dto.ChatMessageResponseDto;
import com.workerconnect.model.Booking;
import com.workerconnect.model.ChatMessage;
import com.workerconnect.model.User;
import com.workerconnect.repository.BookingRepository;
import com.workerconnect.repository.ChatRepository;
import com.workerconnect.repository.UserRepository;
import com.workerconnect.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatService chatService;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter CHAT_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @MessageMapping("/sendMessage")
    public void send(ChatMessageDto dto,Principal principal){

        System.out.println("**********************************");

        String username = principal.getName();

        User sender = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        dto.setSenderId(sender.getId());
        ChatMessage message = chatService.save(dto);
        ChatMessageResponseDto response=new ChatMessageResponseDto();
        response.setId(message.getId());
        response.setMessage(message.getMessage());
        response.setSenderName(sender.getFullName());
        response.setSenderId(sender.getId());
        response.setSentTime(message.getSentTime().format(CHAT_TIME_FORMAT));
        messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getBookingId(),
                response
        );

    }

    @GetMapping("/chat/{bookingId}")
    public String chat(@PathVariable Long bookingId, Model model,Principal principal){

        Booking booking=bookingRepository.findById(bookingId).orElse(null);
        Long receiverId=booking.getWorker().getId();

        String username = principal.getName();

        User sender = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("bookingId", bookingId);

        model.addAttribute("receiverId",receiverId);
        model.addAttribute("currentUserId", sender.getId());

        return "chat/chat";

    }   

    @GetMapping("/chat/messages/{bookingId}")
    @ResponseBody
    public List<ChatMessageResponseDto> messages(@PathVariable Long bookingId){

        List<ChatMessage> msg=chatRepository.findByBookingIdOrderBySentTimeAsc(bookingId);

        List<ChatMessageResponseDto> response=new ArrayList<>();
        for(ChatMessage chat:msg){
            ChatMessageResponseDto dto=new ChatMessageResponseDto();

            dto.setId(chat.getId());
            dto.setMessage(chat.getMessage());
            dto.setSenderId(chat.getSender().getId());
            dto.setSenderName(chat.getSender().getFullName());
            dto.setSentTime(chat.getSentTime().format(CHAT_TIME_FORMAT));
            response.add(dto);

        }
        return response;

    }

}
