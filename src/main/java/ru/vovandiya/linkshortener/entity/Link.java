package ru.vovandiya.linkshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "links",
    indexes = {@Index(name = "idx_link_short_link", columnList = "short_link")})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "generated_by_id")
  private User generatedBy;

  @Column(nullable = false, unique = true)
  private String shortLink;

  @Column(nullable = false, length = 1000)
  private String destination;

  private Integer maxUses;
  private Integer timesUsed = 0;
  private LocalDateTime expiration;
}
