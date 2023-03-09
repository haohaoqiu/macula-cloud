/*
 * Copyright (c) 2023 Macula
 *   macula.dev, China
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.powerjob.server.remote.worker;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.common.request.WorkerHeartbeat;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理 worker 集群信息
 *
 * @author tjq
 * @since 2020/4/5
 */
@Slf4j
public class WorkerClusterManagerService {

    /**
     * 存储Worker健康信息，appId -> ClusterStatusHolder
     */
    private static final Map<Long, ClusterStatusHolder> APP_ID_2_CLUSTER_STATUS = Maps.newConcurrentMap();

    /**
     * 更新状态
     *
     * @param heartbeat Worker的心跳包
     */
    public static void updateStatus(WorkerHeartbeat heartbeat) {
        Long appId = heartbeat.getAppId();
        String appName = heartbeat.getAppName();
        ClusterStatusHolder clusterStatusHolder =
            APP_ID_2_CLUSTER_STATUS.computeIfAbsent(appId, ignore -> new ClusterStatusHolder(appName));
        clusterStatusHolder.updateStatus(heartbeat);
    }

    /**
     * 清理不需要的worker信息
     *
     * @param usingAppIds 需要维护的appId，其余的数据将被删除
     */
    public static void clean(List<Long> usingAppIds) {
        Set<Long> keys = Sets.newHashSet(usingAppIds);
        APP_ID_2_CLUSTER_STATUS.entrySet().removeIf(entry -> !keys.contains(entry.getKey()));
    }

    /**
     * 清理缓存信息，防止 OOM
     */
    public static void cleanUp() {
        APP_ID_2_CLUSTER_STATUS.values().forEach(ClusterStatusHolder::release);
    }

    protected static Map<Long, ClusterStatusHolder> getAppId2ClusterStatus() {
        return APP_ID_2_CLUSTER_STATUS;
    }

}