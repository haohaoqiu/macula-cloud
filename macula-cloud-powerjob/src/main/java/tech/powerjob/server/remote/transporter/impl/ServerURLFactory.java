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

package tech.powerjob.server.remote.transporter.impl;

import tech.powerjob.remote.framework.base.Address;
import tech.powerjob.remote.framework.base.HandlerLocation;
import tech.powerjob.remote.framework.base.ServerType;
import tech.powerjob.remote.framework.base.URL;

import static tech.powerjob.common.RemoteConstant.*;

/**
 * 统一生成地址
 *
 * @author tjq
 * @since 2023/1/21
 */
public class ServerURLFactory {

    public static URL dispatchJob2Worker(String address) {
        return simileBuild(address, ServerType.WORKER, WORKER_PATH, WTT_HANDLER_RUN_JOB);
    }

    public static URL stopInstance2Worker(String address) {
        return simileBuild(address, ServerType.WORKER, WORKER_PATH, WTT_HANDLER_STOP_INSTANCE);
    }

    public static URL queryInstance2Worker(String address) {
        return simileBuild(address, ServerType.WORKER, WORKER_PATH, WTT_HANDLER_QUERY_INSTANCE_STATUS);
    }

    public static URL deployContainer2Worker(String address) {
        return simileBuild(address, ServerType.WORKER, WORKER_PATH, WORKER_HANDLER_DEPLOY_CONTAINER);
    }

    public static URL destroyContainer2Worker(String address) {
        return simileBuild(address, ServerType.WORKER, WORKER_PATH, WORKER_HANDLER_DESTROY_CONTAINER);
    }

    public static URL ping2Friend(String address) {
        return simileBuild(address, ServerType.SERVER, S4S_PATH, S4S_HANDLER_PING);
    }

    public static URL process2Friend(String address) {
        return simileBuild(address, ServerType.SERVER, S4S_PATH, S4S_HANDLER_PROCESS);
    }

    public static URL simileBuild(String address, ServerType type, String rootPath, String handlerPath) {
        return new URL().setServerType(type).setAddress(Address.fromIpv4(address))
            .setLocation(new HandlerLocation().setRootPath(rootPath).setMethodPath(handlerPath));
    }
}