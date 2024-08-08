package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接分组控制层
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
}
