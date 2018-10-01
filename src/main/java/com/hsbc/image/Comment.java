package com.hsbc.image;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Comment {

    @Id private String id;
    private String imageId;
    private String comment;

}
