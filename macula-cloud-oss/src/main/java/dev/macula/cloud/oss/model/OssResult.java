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

package dev.macula.cloud.oss.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 存储返回结果对象
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 *
 * @author hubin
 * @since 2022-06-09
 */
@Getter
@Setter
@Builder
public class OssResult implements Serializable {

    /**
     * 存储桶名
     */
    private String bucketName;

    /**
     * 对象名称
     */
    private String objectName;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 版本
     */
    private String versionId;

}