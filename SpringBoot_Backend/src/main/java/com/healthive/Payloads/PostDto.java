package com.healthive.Payloads;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthive.Models.Category;
import com.healthive.Models.Images;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private long Id;
    private List<Images> postImage = new ArrayList<>();
    private String description;
    private Category category;
    private Long likesCount = 0L;
    private Long commentCount = 0L;
    private String postedOn;
    private UserShow user;
    private Boolean ifLiked = false;
}
