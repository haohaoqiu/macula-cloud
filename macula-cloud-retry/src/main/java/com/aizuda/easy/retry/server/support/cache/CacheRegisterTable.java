package com.aizuda.easy.retry.server.support.cache;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.service.convert.RegisterNodeInfoConverter;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * POD注册表
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 1.6.0
 */
@Component
@Slf4j
public class CacheRegisterTable implements Lifecycle {

    private static Cache<String, ConcurrentMap<String, RegisterNodeInfo>> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getAllPods() {
        ConcurrentMap<String, ConcurrentMap<String, RegisterNodeInfo>> concurrentMap = CACHE.asMap();
        if (CollectionUtils.isEmpty(concurrentMap)) {
            return Collections.EMPTY_SET;
        }

        return concurrentMap.values().stream()
            .map(stringServerNodeConcurrentMap -> new TreeSet(stringServerNodeConcurrentMap.values()))
            .reduce((s, y) -> {
                s.addAll(y);
                return s;
            }).get();

    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ConcurrentMap<String, RegisterNodeInfo> get(String groupName) {
        return CACHE.getIfPresent(groupName);
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static RegisterNodeInfo getServerNode(String groupName, String hostId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return null;
        }

        return concurrentMap.get(hostId);
    }

    /**
     * 获取排序的ServerNode
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getServerNodeSet(String groupName) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (CollectionUtils.isEmpty(concurrentMap)) {
            return Collections.EMPTY_SET;
        }

        return new TreeSet<>(concurrentMap.values());
    }

    /**
     * 获取排序的hostId
     *
     * @return 缓存对象
     */
    public static Set<String> getPodIdSet(String groupName) {
        return getServerNodeSet(groupName).stream().map(RegisterNodeInfo::getHostId).collect(Collectors.toSet());
    }

    /**
     * 刷新过期时间若不存在则初始化
     *
     * @param groupName 组名称
     */
    public static synchronized void refreshExpireAt(String groupName, ServerNode serverNode) {
        RegisterNodeInfo registerNodeInfo = getServerNode(groupName, serverNode.getHostId());
        // 不存在则初始化
        if (Objects.isNull(registerNodeInfo)) {
            LogUtils.warn(log, "node not exists. groupName:[{}] hostId:[{}]", groupName, serverNode.getHostId());
        } else {
            // 存在则刷新过期时间
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());
        }
    }

    /**
     * 无缓存时添加 有缓存时更新
     *
     * @return 缓存对象
     */
    public static synchronized void addOrUpdate(String groupName, ServerNode serverNode) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        RegisterNodeInfo registerNodeInfo;
        if (Objects.isNull(concurrentMap)) {
            LogUtils.info(log, "Add cache. groupName:[{}] hostId:[{}]", groupName, serverNode.getHostId());
            concurrentMap = new ConcurrentHashMap<>();
            registerNodeInfo = RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode);
            CACHE.put(groupName, concurrentMap);
        } else {
            // 复用缓存中的对象
            registerNodeInfo = concurrentMap.getOrDefault(serverNode.getHostId(),
                RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode));
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());
        }

        LogUtils.info(log, "Update cache. groupName:[{}] hostId:[{}] hostIp:[{}] expireAt:[{}]", groupName,
            serverNode.getHostId(), serverNode.getHostIp(), serverNode.getExpireAt());

        concurrentMap.put(serverNode.getHostId(), registerNodeInfo);
    }

    /**
     * 删除缓存
     *
     * @param groupName 组名称
     * @param hostId    机器id
     */
    public static void remove(String groupName, String hostId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return;
        }

        LogUtils.info(log, "Remove cache. groupName:[{}] hostId:[{}]", groupName, hostId);
        concurrentMap.remove(hostId);
    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
            // 设置并发级别为cpu核心数
            .concurrencyLevel(Runtime.getRuntime().availableProcessors()).build();

    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
