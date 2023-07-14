package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.service.DashBoardService;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.easy.retry.server.web.model.response.TaskQuantityResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 看板接口
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:17
 */
@RestController
@RequestMapping("/dashboard")
public class DashBoardController {

    @Autowired
    private DashBoardService dashBoardService;

    @LoginRequired
    @GetMapping("/task/count")
    public TaskQuantityResponseVO countTask() {
        return dashBoardService.countTask();
    }

    @LoginRequired
    @GetMapping("/dispatch/count")
    public DispatchQuantityResponseVO countDispatch() {
        return dashBoardService.countDispatch();
    }

    @LoginRequired
    @GetMapping("/active-pod/count")
    public ActivePodQuantityResponseVO countActivePod() {
        return dashBoardService.countActivePod();
    }

    @LoginRequired
    @GetMapping("/scene/rank")
    public List<SceneQuantityRankResponseVO> rankSceneQuantity(
        @RequestParam(value = "groupName", required = false) String groupName,
        @RequestParam(value = "type") String type,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "endTime", required = false) String endTime) {
        return dashBoardService.rankSceneQuantity(groupName, type, startTime, endTime);
    }

    @LoginRequired
    @GetMapping("/dispatch/line")
    public List<DispatchQuantityResponseVO> lineDispatchQuantity(
        @RequestParam(value = "groupName", required = false) String groupName,
        @RequestParam(value = "type") String type,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "endTime", required = false) String endTime) {
        return dashBoardService.lineDispatchQuantity(groupName, type, startTime, endTime);
    }

    @LoginRequired
    @GetMapping("/pods")
    public PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO serverNodeQueryVO) {
        return dashBoardService.pods(serverNodeQueryVO);
    }

    @GetMapping("/consumer/group")
    public Set<String> allConsumerGroupName() {
        return CacheConsumerGroup.getAllConsumerGroupName();
    }

}
