package dev.macula.cloud.system.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SysTenantDict {
    private Long dictItemId;
    private Long tenantId;
}
