package com.nageoffer.shortlink.project.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LinkAccessLogsMapper 的selectUvTypeByUsers穿参对象
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserAccessLogParam {
    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 用户集合
     */
    private List<String> userAccessLogsList;

    /**
     * 启用标识 0：启用 1：未启用
     */
    private Integer enableStatus;


}
