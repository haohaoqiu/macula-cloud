package com.aizuda.easy.retry.server.persistence.support.access.config;

import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.GroupConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.NotifyConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.SceneConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 获取配置通道通用模板
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 09:12
 */
public abstract class AbstractConfigAccess implements ConfigAccess {

    @Autowired
    protected NotifyConfigMapper notifyConfigMapper;
    @Autowired
    protected SceneConfigMapper sceneConfigMapper;
    @Autowired
    protected GroupConfigMapper groupConfigMapper;

    protected List<NotifyConfig> getByGroupIdAndNotifyScene(String groupName, Integer notifyScene) {
        return notifyConfigMapper.selectList(
            new LambdaQueryWrapper<NotifyConfig>().eq(NotifyConfig::getGroupName, groupName)
                .eq(NotifyConfig::getNotifyScene, notifyScene));
    }

    protected SceneConfig getByGroupNameAndSceneName(String groupName, String sceneName) {
        return sceneConfigMapper.selectOne(
            new LambdaQueryWrapper<SceneConfig>().eq(SceneConfig::getGroupName, groupName)
                .eq(SceneConfig::getSceneName, sceneName));
    }

    protected List<SceneConfig> getSceneConfigs(String groupName) {
        return sceneConfigMapper.selectList(
            new LambdaQueryWrapper<SceneConfig>().eq(SceneConfig::getGroupName, groupName));
    }

    protected GroupConfig getByGroupName(String groupName) {
        return groupConfigMapper.selectOne(
            new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupName));
    }

    protected List<NotifyConfig> getNotifyConfigs(String groupName) {
        return notifyConfigMapper.selectList(
            new LambdaQueryWrapper<NotifyConfig>().eq(NotifyConfig::getGroupName, groupName));
    }

    @Override
    public Integer getConfigVersion(String groupName) {
        return null;
    }

    @Override
    public ConfigDTO getConfigInfo(String groupName) {
        return null;
    }
}
