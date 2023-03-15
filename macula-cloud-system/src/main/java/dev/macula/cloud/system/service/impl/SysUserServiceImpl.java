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

package dev.macula.cloud.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import dev.macula.boot.base.IBaseEnum;
import dev.macula.boot.constants.SecurityConstants;
import dev.macula.boot.enums.GenderEnum;
import dev.macula.boot.starter.security.utils.SecurityUtils;
import dev.macula.cloud.system.converter.UserConverter;
import dev.macula.cloud.system.dto.UserAuthInfo;
import dev.macula.cloud.system.dto.UserImportDTO;
import dev.macula.cloud.system.form.UserForm;
import dev.macula.cloud.system.listener.UserImportListener;
import dev.macula.cloud.system.mapper.SysUserMapper;
import dev.macula.cloud.system.pojo.bo.UserBO;
import dev.macula.cloud.system.pojo.bo.UserFormBO;
import dev.macula.cloud.system.pojo.entity.SysUser;
import dev.macula.cloud.system.pojo.entity.SysUserRole;
import dev.macula.cloud.system.query.UserPageQuery;
import dev.macula.cloud.system.service.SysMenuService;
import dev.macula.cloud.system.service.SysRoleService;
import dev.macula.cloud.system.service.SysUserRoleService;
import dev.macula.cloud.system.service.SysUserService;
import dev.macula.cloud.system.vo.user.UserExportVO;
import dev.macula.cloud.system.vo.user.UserLoginVO;
import dev.macula.cloud.system.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务实现类
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleService userRoleService;
    private final UserImportListener userImportListener;
    private final UserConverter userConverter;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final RedisTemplate redisTemplate;

    /**
     * 根据给定的用户名获取登录信息，不含角色
     *
     * @param username 用户名
     * @param roles    该用户的角色
     * @return 登录用户信息
     */
    public UserLoginVO getUserInfo(String username, Set<String> roles) {
        // 登录用户entity
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
            .select(SysUser::getId, SysUser::getUsername, SysUser::getNickname, SysUser::getAvatar));
        // entity->VO
        UserLoginVO userLoginVO = userConverter.entity2LoginUser(user);

        // 用户角色集合
        userLoginVO.setRoles(roles);

        // 用户权限集合
        Set<String> perms =
            (Set<String>)redisTemplate.opsForValue().get(SecurityConstants.SECURITY_USER_BTN_PERMS_KEY + username);
        if (perms == null) {
            perms = menuService.listRolePerms(roles);
            redisTemplate.opsForValue().set(SecurityConstants.SECURITY_USER_BTN_PERMS_KEY + username, perms);
        }
        userLoginVO.setPerms(perms);

        return userLoginVO;
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 登录用户的角色和权限等信息
     */
    @Override
    public UserLoginVO getCurrentUserInfo() {
        return getUserInfo(SecurityUtils.getCurrentUser(), SecurityUtils.getRoles());
    }

    /**
     * 根据用户名获取认证信息(给oauth2调用的，带了密码，不要给其他应用访问）
     *
     * @param username
     * @return
     */
    @Override
    public UserAuthInfo getUserAuthInfo(String username) {
        UserAuthInfo userAuthInfo = this.baseMapper.getUserAuthInfo(username);

        Set<String> roles = userAuthInfo.getRoles();
        if (CollectionUtil.isNotEmpty(roles)) {
            // 每次被调用也就是用户登录的时候，更新按钮权限缓存
            Set<String> perms = menuService.listRolePerms(roles);
            redisTemplate.opsForValue().set(SecurityConstants.SECURITY_USER_BTN_PERMS_KEY + username, perms);

            // 获取最大范围的数据权限
            Integer dataScope = roleService.getMaximumDataScope(roles);
            userAuthInfo.setDataScope(dataScope);
        }
        return userAuthInfo;
    }

    /**
     * 获取用户分页列表
     *
     * @param queryParams
     * @return
     */
    @Override
    public IPage<UserVO> listUserPages(UserPageQuery queryParams) {

        // 查询数据
        Page<UserBO> userBoPage =
            this.baseMapper.listUserPages(new Page<>(queryParams.getPageNum(), queryParams.getPageSize()), queryParams);

        // 实体转换
        Page<UserVO> userVoPage = userConverter.bo2Vo(userBoPage);

        return userVoPage;
    }

    /**
     * 获取用户详情
     *
     * @param userId
     * @return
     */
    @Override
    public UserForm getUserFormData(Long userId) {
        UserFormBO userFormBO = this.baseMapper.getUserDetail(userId);
        // 实体转换po->form
        UserForm userForm = userConverter.bo2Form(userFormBO);
        return userForm;
    }

    /**
     * 新增用户
     *
     * @param userForm 用户表单对象
     * @return
     */
    @Override
    public boolean saveUser(UserForm userForm) {

        String username = userForm.getUsername();

        long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        Assert.isTrue(count == 0, "用户名已存在");

        // 实体转换 form->entity
        SysUser entity = userConverter.form2Entity(userForm);

        // 加密密码
        String encryptPwd = passwordEncoder.encode(userForm.getPassword());
        entity.setPassword(encryptPwd);

        // 新增用户
        boolean result = this.save(entity);

        if (result) {
            // 保存用户角色
            userRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;
    }

    /**
     * 更新用户
     *
     * @param userId   用户ID
     * @param userForm 用户表单对象
     * @return
     */
    @Override
    @Transactional
    public boolean updateUser(Long userId, UserForm userForm) {

        String username = userForm.getUsername();

        long count =
            this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username).ne(SysUser::getId, userId));
        Assert.isTrue(count == 0, "用户名已存在");

        // form -> entity
        SysUser entity = userConverter.form2Entity(userForm);

        // 修改用户
        boolean result = this.updateById(entity);

        if (result) {
            // 保存用户角色
            userRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;
    }

    /**
     * 删除用户
     *
     * @param idsStr 用户ID，多个以英文逗号(,)分割
     * @return
     */
    @Override
    public boolean deleteUsers(String idsStr) {
        Assert.isTrue(StrUtil.isNotBlank(idsStr), "删除的用户数据为空");
        // 逻辑删除
        List<Long> ids =
            Arrays.asList(idsStr.split(",")).stream().map(idStr -> Long.parseLong(idStr)).collect(Collectors.toList());
        boolean result = this.removeByIds(ids);
        return result;

    }

    /**
     * 修改用户密码
     *
     * @param userId   用户ID
     * @param password 用户密码
     * @return
     */
    @Override
    public boolean updatePassword(Long userId, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        boolean result = this.update(
            new LambdaUpdateWrapper<SysUser>().eq(SysUser::getId, userId).set(SysUser::getPassword, encryptedPassword));

        return result;
    }

    /**
     * 导入用户
     *
     * @param userImportDTO
     * @return
     */
    @Transactional
    @Override
    public String importUsers(UserImportDTO userImportDTO) throws IOException {

        Long deptId = userImportDTO.getDeptId();
        List<Long> roleIds = Arrays.stream(userImportDTO.getRoleIds().split(",")).map(roleId -> Convert.toLong(roleId))
            .collect(Collectors.toList());
        InputStream inputStream = userImportDTO.getFile().getInputStream();

        ExcelReaderBuilder excelReaderBuilder =
            EasyExcel.read(inputStream, UserImportDTO.UserItem.class, userImportListener);
        ExcelReaderSheetBuilder sheet = excelReaderBuilder.sheet();
        List<UserImportDTO.UserItem> list = sheet.doReadSync();

        Assert.isTrue(CollectionUtil.isNotEmpty(list), "未检测到任何数据");

        // 有效数据集合
        List<UserImportDTO.UserItem> validDataList =
            list.stream().filter(item -> StrUtil.isNotBlank(item.getUsername())).collect(Collectors.toList());

        Assert.isTrue(CollectionUtil.isNotEmpty(validDataList), "未检测到有效数据");

        long distinctCount = validDataList.stream().map(UserImportDTO.UserItem::getUsername).distinct().count();
        Assert.isTrue(validDataList.size() == distinctCount, "导入数据中有重复的用户名，请检查！");

        List<SysUser> saveUserList = Lists.newArrayList();

        StringBuilder errMsg = new StringBuilder();
        for (int i = 0; i < validDataList.size(); i++) {
            UserImportDTO.UserItem userItem = validDataList.get(i);

            String username = userItem.getUsername();
            if (StrUtil.isBlank(username)) {
                errMsg.append(StrUtil.format("第{}条数据导入失败，原因：用户名为空", i + 1));
                continue;
            }

            String nickname = userItem.getNickname();
            if (StrUtil.isBlank(nickname)) {
                errMsg.append(StrUtil.format("第{}条数据导入失败，原因：用户昵称为空", i + 1));
                continue;
            }

            SysUser user = new SysUser();
            user.setUsername(username);
            user.setNickname(nickname);
            user.setMobile(userItem.getMobile());
            user.setEmail(userItem.getEmail());
            user.setDeptId(deptId);
            // 默认密码
            user.setPassword(passwordEncoder.encode(SecurityConstants.DEFAULT_USER_PASSWORD));
            // 性别转换
            Integer gender = (Integer)IBaseEnum.getValueByLabel(userItem.getGender(), GenderEnum.class);
            user.setGender(gender);

            saveUserList.add(user);
        }

        if (CollectionUtil.isNotEmpty(saveUserList)) {
            boolean result = this.saveBatch(saveUserList);
            Assert.isTrue(result, "导入数据失败，原因：保存用户出错");

            List<SysUserRole> userRoleList = new ArrayList<>();

            if (CollectionUtil.isNotEmpty(roleIds)) {

                roleIds.forEach(roleId -> {
                    userRoleList.addAll(saveUserList.stream().map(user -> new SysUserRole(user.getId(), roleId))
                        .collect(Collectors.toList()));
                });
            }

            userRoleService.saveBatch(userRoleList);
        }

        errMsg.append(StrUtil.format("一共{}条数据，成功导入{}条数据，导入失败数据{}条", list.size(), saveUserList.size(),
            list.size() - saveUserList.size()));
        return errMsg.toString();

    }

    /**
     * 获取导出用户列表
     *
     * @param queryParams
     * @return
     */
    @Override
    public List<UserExportVO> listExportUsers(UserPageQuery queryParams) {
        List<UserExportVO> list = this.baseMapper.listExportUsers(queryParams);
        return list;
    }

    @Override
    public IPage<UserVO> listUserPagesByIds(UserPageQuery queryParams) {

        // 查询数据
        Page<UserBO> userBoPage =
            this.baseMapper.listUserPagesByIds(new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams);

        // 实体转换
        Page<UserVO> userVoPage = userConverter.bo2Vo(userBoPage);

        return userVoPage;
    }
}
