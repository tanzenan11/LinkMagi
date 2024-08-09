package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 短链接分组接口实现层
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
    /**
     * 新增短链接分组
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String userName, String groupName) {
        String randomGid;
        while (true){
            randomGid = RandomGenerator.generateRandom();
            if(!hasGid(userName,randomGid)){
                // 代表不存在
                break;
            }
        }
        GroupDO groupDO = GroupDO.builder()
                .gid(randomGid)
                .name(groupName)
                .sortOrder(0)
                .username(userName)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 查询用户短链接分组集合
   */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        // 创建查询条件构造器，查询未被删除的、当前用户创建的短链接分组，并按排序字段和更新时间倒序排列
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)  // 查询未被删除的分组
                .eq(GroupDO::getUsername, UserContext.getUsername())  // 仅查询当前用户的分组
                .orderByDesc(List.of(GroupDO::getSortOrder, GroupDO::getUpdateTime));  // 按排序顺序和更新时间倒序排列

        // 执行查询，获取分组列表
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        // 将查询到的分组列表转换为响应 DTO 列表
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
        // 调用远程服务，获取每个分组中短链接的计数信息
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService
                .listGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());
        // 遍历每个分组的响应 DTO，并将对应的短链接计数信息填充到 DTO 中
        shortLinkGroupRespDTOList.forEach(each -> {
            // 找到当前分组对应的短链接计数信息
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            // 如果找到对应的计数信息，将计数赋值给 DTO
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });
        // 返回填充了短链接计数信息的分组列表
        return shortLinkGroupRespDTOList;
    }


    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO,updateWrapper);
    }

    /**
     * 短链接分组排序
     */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .set(GroupDO::getSortOrder, each.getSortOrder())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            update(updateWrapper);
        });
    }

    private boolean hasGid(String userName,String randomGid) {
         //获取随机6位数字
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, randomGid)
                .eq(GroupDO::getUsername, Optional.ofNullable(userName).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);

        return hasGroupFlag != null;
    }
}
