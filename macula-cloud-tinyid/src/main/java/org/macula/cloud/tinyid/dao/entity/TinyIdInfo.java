package org.macula.cloud.tinyid.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author du_imba
 */
@Data
public class TinyIdInfo {
    private Long id;

    private String bizType;

    private Long beginId;

    private Long maxId;

    private Integer step;

    private Integer delta;

    private Integer remainder;

    private Date createTime;

    private Date updateTime;

    private Long version;

}