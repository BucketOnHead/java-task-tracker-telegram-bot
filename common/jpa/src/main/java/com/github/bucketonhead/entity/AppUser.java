package com.github.bucketonhead.entity;

import com.github.bucketonhead.entity.enums.BotState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "app_user")
@Builder
@EqualsAndHashCode(exclude = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramUserId;

    @CreationTimestamp
    private LocalDateTime firstLoginDate;

    private String firstName;
    private String lastName;
    private String username;
    private String email;

    @Enumerated(EnumType.STRING)
    private BotState state;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private List<AppTask> tasks;
}
