package com.workerconnect.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ChatMessage {


    @Id
    @GeneratedValue(strategy  = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;


    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "reciever_id")
    private User reciever;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime sentTime;

    private boolean seen;

}
