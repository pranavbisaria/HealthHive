package com.healthive.Payloads;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthive.Models.Post;
import com.healthive.Models.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommentsDto {
    private Long Id;
    private Long likes = 0L;
    private String description;
    private String postedOn;
    private UserShow user;
    private Boolean ifLiked;
}
