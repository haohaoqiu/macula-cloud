/*
 * Copyright (c) 2022 Macula
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

package dev.macula.cloud.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.macula.boot.result.Option;
import dev.macula.cloud.system.form.RoleForm;
import dev.macula.cloud.system.pojo.entity.SysRole;
import dev.macula.cloud.system.query.RolePageQuery;
import dev.macula.cloud.system.service.SysPermissionService;
import dev.macula.cloud.system.service.SysRoleService;
import dev.macula.cloud.system.vo.role.RolePageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@Tag(name = "角色接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {
    private final SysRoleService roleService;

    private final SysPermissionService sysPermissionService;

    @Operation(summary = "角色分页列表")
    @GetMapping("/pages")
    public IPage<RolePageVO> listRolePages(RolePageQuery queryParams) {
        IPage<RolePageVO> result = roleService.listRolePages(queryParams);
        return result;
    }

    @Operation(summary = "角色下拉列表")
    @GetMapping("/options")
    public List<Option> listRoleOptions() {
        List<Option> list = roleService.listRoleOptions();
        return list;
    }

    @Operation(summary = "角色详情")
    @Parameter(name = "角色ID")
    @GetMapping("/{roleId}")
    public SysRole getRoleDetail(
            @PathVariable Long roleId
    ) {
        SysRole role = roleService.getById(roleId);
        return role;
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public boolean addRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = roleService.saveRole(roleForm);
        return result;
    }

    @Operation(summary = "修改角色")
    @PutMapping(value = "/{id}")
    public boolean updateRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = roleService.saveRole(roleForm);
        if (result) {
            sysPermissionService.refreshPermRolesRules();
        }
        return result;
    }

    @Operation(summary = "删除角色")
    @Parameter(description = "删除角色，多个以英文逗号(,)分割")
    @DeleteMapping("/{ids}")
    public boolean deleteRoles(
            @PathVariable String ids
    ) {
        boolean result = roleService.deleteRoles(ids);
        return result;
    }

    @Operation(summary = "修改角色状态")
    @Parameter(name = "角色ID")
    @Parameter(name = "角色状态", description = "角色状态:1-启用；0-禁用")
    @PutMapping(value = "/{roleId}/status")
    public boolean updateRoleStatus(
            @PathVariable Long roleId,
            @RequestParam Integer status
    ) {
        boolean result = roleService.updateRoleStatus(roleId, status);
        return result;
    }

    @Operation(summary = "获取角色的菜单ID集合")
    @Parameter(name = "角色ID")
    @GetMapping("/{roleId}/menuIds")
    public List<Long> getRoleMenuIds(
            @PathVariable Long roleId
    ) {
        List<Long> resourceIds = roleService.getRoleMenuIds(roleId);
        return resourceIds;
    }

    @Operation(summary = "分配角色的资源权限")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "更新角色菜单的请求体对象,先删除当前分页【curPage】(没有则全部删除)该角色拥有的菜单信息," +
                    "后添加当前选择的菜单信息," +
                    "curPage(当前分页菜单列表中所有菜单id列表,可不传)," +
                    "curSel（当前页选择添加给角色的菜单id列表）",
            content = {@Content(mediaType="application/json",
                    schema = @Schema(implementation = Object.class, requiredProperties = "curSel"),
                    schemaProperties = {
                            @SchemaProperty(name = "curPage", array= @ArraySchema(arraySchema = @Schema(implementation = List.class), schema = @Schema(implementation = Long.class))),
                            @SchemaProperty(name = "curSel", array= @ArraySchema(arraySchema = @Schema(implementation = List.class), schema = @Schema(implementation = Long.class)))}
            )}
    )
    @PutMapping("/{roleId}/menus")
    public boolean updateRoleMenus(
            @PathVariable Long roleId,
            @RequestBody Map<String,List<Long>> menuIds
    ) {
        boolean result = roleService.updateRoleMenus(roleId, menuIds);
        if (result) {
            sysPermissionService.refreshPermRolesRules();
        }
        return result;
    }

    @Operation(summary = "角色编码值验证器")
    @Parameter(name = "角色id", description = "允许为空，新增时为空，编辑时携带")
    @Parameter(name = "角色编码")
    @GetMapping("/validtor/code")
    public boolean validtorForCode(@RequestParam(required = false) Long id, @RequestParam String code) {
        return roleService.validtorForCode(id, code);
    }

    @Operation(summary = "角色名称值验证器")
    @Parameter(name = "角色id", description = "允许为空，新增时为空，编辑时携带")
    @Parameter(name = "角色名称")
    @GetMapping("/validtor/name")
    public boolean validtorForName(@RequestParam(required = false) Long id, @RequestParam String name){
        return roleService.validtorForName(id, name);
    }

    @Operation(summary = "获取数据权限的下拉列表")
    @GetMapping("/optionsByDataScope")
    public List<Option> optionsByDataScope() {
        return roleService.optionsByDataScope();
    }

    @Operation(summary = "获取角色的权限id集合")
    @Parameter(name = "角色ID")
    @GetMapping("/{roleId}/permIds")
    public List<Long> getRolePermIds(@PathVariable("roleId") Long roleId){
        List<Long> resourcePermIds = roleService.getRolePermIds(roleId);
        return resourcePermIds;
    }

    @Operation(summary = "分配角色的路径权限")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "更新角色权限的请求体对象,先删除当前分页【curPage】(没有则全部删除)该角色拥有的权限信息," +
                    "后添加当前选择的权限信息," +
                    "curPage(当前分页权限列表中所有权限id列表,可不传)," +
                    "curSel（当前页选择添加给角色的权限id列表）",
            content = {@Content(mediaType="application/json",
                    schema = @Schema(implementation = Object.class, requiredProperties = "curSel"),
                    schemaProperties = {
                            @SchemaProperty(name = "curPage", array= @ArraySchema(arraySchema = @Schema(implementation = List.class), schema = @Schema(implementation = Long.class))),
                            @SchemaProperty(name = "curSel", array= @ArraySchema(arraySchema = @Schema(implementation = List.class), schema = @Schema(implementation = Long.class)))}
            )}
    )
    @PutMapping("/{roleId}/perms")
    public boolean updateRolePerms(
            @PathVariable Long roleId,
            @RequestBody Map<String,List<Long>> permIds
    ) {
        boolean result = roleService.updateRolePerms(roleId, permIds);
        if (result) {
            sysPermissionService.refreshPermRolesRules();
        }
        return result;
    }
}
