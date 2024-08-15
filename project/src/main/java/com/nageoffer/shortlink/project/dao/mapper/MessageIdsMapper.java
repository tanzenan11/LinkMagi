package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface MessageIdsMapper extends BaseMapper<LinkStatsTodayDO> {
    /**
     * 将该数据插入数据库
     */
    @Insert("INSERT INTO message_ids (id) VALUES (#{messageId})")
    void saveMessageId(String messageId);

    /**
     * 根据id判断该数据是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM message_ids WHERE id = #{messageId}")
    boolean existsById(String messageId);

    /**
     * 清除数据表中的数据
     */
    @Delete("DELETE FROM message_ids")
    void deleteAllMessageIds();
}

