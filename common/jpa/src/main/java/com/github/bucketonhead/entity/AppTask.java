package com.github.bucketonhead.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "app_task")
@Builder
@EqualsAndHashCode(exclude = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    private AppUser creator;
}