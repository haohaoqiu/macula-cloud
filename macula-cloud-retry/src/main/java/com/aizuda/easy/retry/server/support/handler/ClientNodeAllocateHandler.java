package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.allocate.client.ClientLoadBalanceManager;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aizuda.easy.retry.server.support.ClientLoadBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2023-01-10 14:18
 */
@Component
public class ClientNodeAllocateHandler {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    /**
     * 获取分配的节点
     */
    public RegisterNodeInfo getServerNode(String groupName) {

        GroupConfig groupConfig = configAccess.getGroupConfigByGroupName(groupName);
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(groupName);
        if (CollectionUtils.isEmpty(serverNodes)) {
            return null;
        }

        ClientLoadBalance clientLoadBalanceRandom =
            ClientLoadBalanceManager.getClientLoadBalance(groupConfig.getRouteKey());

        String hostIp = clientLoadBalanceRandom.route(groupName,
            new TreeSet<>(serverNodes.stream().map(RegisterNodeInfo::getHostIp).collect(Collectors.toSet())));
        return serverNodes.stream().filter(s -> s.getHostIp().equals(hostIp)).findFirst().get();
    }

}
