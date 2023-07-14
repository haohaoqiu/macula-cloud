package com.aizuda.easy.retry.server;

import com.aizuda.easy.retry.server.support.handler.ConfigVersionSyncHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-02 22:38
 */
@SpringBootTest
public class ConfigVersionSyncHandlerTest {

    @Autowired
    private ConfigVersionSyncHandler configVersionSyncHandler;

    @SneakyThrows
    @Test
    public void syncVersion() {
        configVersionSyncHandler.addSyncTask("example_group", 0);

    }

}
