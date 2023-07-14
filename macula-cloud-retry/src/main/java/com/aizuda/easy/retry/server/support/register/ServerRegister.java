package com.aizuda.easy.retry.server.support.register;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.dto.ServerNodeExtAttrs;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Register;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.handler.ServerNodeBalance;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务端注册
 *
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 1.6.0
 */
@Component(ServerRegister.BEAN_NAME)
@Slf4j
public class ServerRegister extends AbstractRegister {
    public static final String BEAN_NAME = "serverRegister";
    private final ScheduledExecutorService serverRegisterNode =
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "server-register-node"));
    public static final int DELAY_TIME = 30;
    public static final String CURRENT_CID;
    public static final String GROUP_NAME = "DEFAULT_SERVER";

    @Autowired
    public ServerNodeBalance serverNodeBalance;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private ServerProperties serverProperties;

    static {
        CURRENT_CID = IdUtil.getSnowflakeNextIdStr();
    }

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }

    @Override
    protected void beforeProcessor(RegisterContext context) {
        // 新增扩展参数
        ServerNodeExtAttrs serverNodeExtAttrs = new ServerNodeExtAttrs();
        serverNodeExtAttrs.setWebPort(serverProperties.getPort());

        context.setGroupName(GROUP_NAME);
        context.setHostId(CURRENT_CID);
        context.setHostIp(HostUtils.getIp());
        context.setHostPort(systemProperties.getNettyPort());
        context.setContextPath(StrUtil.EMPTY);
        context.setExtAttrs(JsonUtil.toJsonString(serverNodeExtAttrs));
    }

    @Override
    protected LocalDateTime getExpireAt(RegisterContext context) {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        refreshExpireAt(serverNode);
        return Boolean.TRUE;
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.SERVER.getType();
    }

    @Override
    public void start() {
        LogUtils.info(log, "ServerRegister start");

        Register register = SpringContext.getBean(ServerRegister.BEAN_NAME, Register.class);
        serverRegisterNode.scheduleAtFixedRate(() -> {
            register.register(new RegisterContext());
        }, 0, DELAY_TIME / 2, TimeUnit.SECONDS);

    }

    @Override
    public void close() {
        LogUtils.info(log, "ServerRegister close");
    }
}
