package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息表实例
 */
@Data
@TableName("message_ids")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageIdDo {
    /**
     * 消息id
     */
    private String id;
}
