package com.aizuda.easy.retry.server.service.impl;

import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLogMessage;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.service.RetryTaskLogService;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:10
 */
@Service
public class RetryTaskLogServiceImpl implements RetryTaskLogService {

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO) {

        PageDTO<RetryTaskLog> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLog> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getGroupName, queryVO.getGroupName());
        }
        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getSceneName, queryVO.getSceneName());
        }
        if (StringUtils.isNotBlank(queryVO.getBizNo())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getBizNo, queryVO.getBizNo());
        }
        if (StringUtils.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getUniqueId, queryVO.getUniqueId());
        }
        if (StringUtils.isNotBlank(queryVO.getIdempotentId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLog::getIdempotentId, queryVO.getIdempotentId());
        }

        retryTaskLogLambdaQueryWrapper.select(RetryTaskLog::getGroupName, RetryTaskLog::getId,
            RetryTaskLog::getSceneName, RetryTaskLog::getIdempotentId, RetryTaskLog::getBizNo,
            RetryTaskLog::getRetryStatus, RetryTaskLog::getCreateDt, RetryTaskLog::getUniqueId,
            RetryTaskLog::getTaskType);
        PageDTO<RetryTaskLog> retryTaskLogPageDTO = retryTaskLogMapper.selectPage(pageDTO,
            retryTaskLogLambdaQueryWrapper.orderByDesc(RetryTaskLog::getCreateDt));

        return new PageResult<>(retryTaskLogPageDTO,
            RetryTaskLogResponseVOConverter.INSTANCE.batchConvert(retryTaskLogPageDTO.getRecords()));
    }

    @Override
    public PageResult<List<RetryTaskLogMessageResponseVO>> getRetryTaskLogMessagePage(
        RetryTaskLogMessageQueryVO queryVO) {

        PageDTO<RetryTaskLogMessage> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<RetryTaskLogMessage> retryTaskLogLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLogMessage::getGroupName, queryVO.getGroupName());
        }
        if (StringUtils.isNotBlank(queryVO.getUniqueId())) {
            retryTaskLogLambdaQueryWrapper.eq(RetryTaskLogMessage::getUniqueId, queryVO.getUniqueId());
        }

        PageDTO<RetryTaskLogMessage> retryTaskLogPageDTO = retryTaskLogMessageMapper.selectPage(pageDTO,
            retryTaskLogLambdaQueryWrapper.orderByDesc(RetryTaskLogMessage::getCreateDt));

        return new PageResult<>(retryTaskLogPageDTO,
            RetryTaskLogResponseVOConverter.INSTANCE.toRetryTaskLogMessageResponseVO(retryTaskLogPageDTO.getRecords()));

    }

    @Override
    public RetryTaskLogResponseVO getRetryTaskLogById(Long id) {
        RetryTaskLog retryTaskLog = retryTaskLogMapper.selectById(id);
        return RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTaskLog);
    }
}
