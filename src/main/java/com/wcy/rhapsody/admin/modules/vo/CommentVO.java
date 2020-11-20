package com.wcy.rhapsody.admin.modules.vo;

import lombok.Data;

import java.util.Date;

/**
 * 评论VO
 *
 * @author Yeeep 2020/11/13
 */
@Data
public class CommentVO {
    private String id;

    private String content;

    private String topicId;

    private String userId;

    private String avatar;

    private String alias;

    private Date createTime;

    private String commentId;

}