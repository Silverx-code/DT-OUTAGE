package com.gridline.dtoutage.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "challenge_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Integer challengeId;

    @Column(name = "challenge_name", nullable = false, unique = true)
    private String challengeName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
