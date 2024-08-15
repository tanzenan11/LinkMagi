package com.nageoffer.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        // 创建一个空的 FlowRule 规则列表
        List<FlowRule> rules = new ArrayList<>();

        // 创建一个新的 FlowRule 实例
        FlowRule createOrderRule = new FlowRule();

        // 设置 FlowRule 的资源名称为 "create_short-link"
        // 这个资源名称用于标识流控规则所适用的资源
        createOrderRule.setResource("create_short-link");

        // 设置流控规则的类型为 QPS（每秒请求数）
        // RuleConstant.FLOW_GRADE_QPS 指定了流控的等级
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);

        // 设置流控规则的 QPS 限制为 1
        // 这意味着每秒最多允许 1 个请求通过
        createOrderRule.setCount(1);

        // 将创建的 FlowRule 添加到规则列表中
        rules.add(createOrderRule);

        // 加载规则到 FlowRuleManager 中
        // FlowRuleManager 用于管理和应用流控规则
        FlowRuleManager.loadRules(rules);
    }

}
