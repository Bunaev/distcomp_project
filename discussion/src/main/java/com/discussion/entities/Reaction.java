package com.discussion.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "tbl_reaction")
public class Reaction {
    @PrimaryKey
    private Long id;
    private Long articleId;
    private String content;
    private ReactionState state = ReactionState.PENDING;
}

