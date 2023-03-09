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

package tech.powerjob.server.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.server.common.aware.ServerInfoAware;
import tech.powerjob.server.common.module.ServerInfo;
import tech.powerjob.server.remote.server.self.ServerInfoService;

import java.util.List;

/**
 * ServerInfoAwareProcessor
 *
 * @author tjq
 * @since 2022/9/12
 */
@Slf4j
@Component
public class ServerInfoAwareProcessor {

    public ServerInfoAwareProcessor(ServerInfoService serverInfoService, List<ServerInfoAware> awareList) {
        final ServerInfo serverInfo = serverInfoService.fetchServiceInfo();
        log.info("[ServerInfoAwareProcessor] current server info: {}", serverInfo);
        awareList.forEach(aware -> {
            aware.setServerInfo(serverInfo);
            log.info("[ServerInfoAwareProcessor] set ServerInfo for: {} successfully", aware);
        });
    }
}