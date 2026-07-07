package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.dto.LoginRequest;
import com.creditbank.platform.dto.LoginResponse;
import com.creditbank.platform.dto.RegisterRequest;
import com.creditbank.platform.dto.UserInfoVO;
import com.creditbank.platform.entity.CreditAccount;
import com.creditbank.platform.entity.IntegrityScore;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.CreditAccountMapper;
import com.creditbank.platform.mapper.IntegrityScoreMapper;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final CreditAccountMapper creditAccountMapper;
    private final IntegrityScoreMapper integrityScoreMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        UserInfoVO userInfo = buildUserInfo(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return LoginResponse.builder().token(token).userInfo(userInfo).build();
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (request.getRoleType() != UserRole.STUDENT && request.getRoleType() != UserRole.ENTERPRISE) {
            throw new BusinessException("仅支持学员或企业用户注册");
        }

        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已存在");
        }

        Long orgId = null;
        if (request.getRoleType() == UserRole.ENTERPRISE) {
            if (!StringUtils.hasText(request.getOrgName())) {
                throw new BusinessException("企业名称不能为空");
            }
            String orgCode = StringUtils.hasText(request.getOrgCode())
                    ? request.getOrgCode()
                    : "ENT_" + System.currentTimeMillis();

            Long codeExists = orgMapper.selectCount(new LambdaQueryWrapper<SysOrganization>()
                    .eq(SysOrganization::getCode, orgCode));
            if (codeExists > 0) {
                throw new BusinessException("企业编码已存在");
            }

            SysOrganization org = new SysOrganization();
            org.setName(request.getOrgName());
            org.setCode(orgCode);
            org.setType(3);
            org.setIntro(request.getOrgIntro());
            org.setContact(StringUtils.hasText(request.getOrgContact()) ? request.getOrgContact() : request.getRealName());
            org.setPhone(StringUtils.hasText(request.getOrgPhone()) ? request.getOrgPhone() : request.getPhone());
            org.setEmail(request.getEmail());
            org.setJoinStatus(1);
            org.setStatus(1);
            orgMapper.insert(org);
            orgId = org.getId();
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRoleType());
        user.setOrgId(orgId);
        user.setStatus(1);
        userMapper.insert(user);

        if (request.getRoleType() == UserRole.STUDENT) {
            initStudentAccount(user.getId());
        }

        UserInfoVO userInfo = buildUserInfo(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return LoginResponse.builder().token(token).userInfo(userInfo).build();
    }

    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return buildUserInfo(user);
    }

    private void initStudentAccount(Long userId) {
        CreditAccount account = new CreditAccount();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalEarned(BigDecimal.ZERO);
        account.setTotalSpent(BigDecimal.ZERO);
        creditAccountMapper.insert(account);

        IntegrityScore score = new IntegrityScore();
        score.setUserId(userId);
        score.setScore(100);
        integrityScoreMapper.insert(score);
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        String orgName = null;
        if (user.getOrgId() != null) {
            SysOrganization org = orgMapper.selectById(user.getOrgId());
            if (org != null) {
                orgName = org.getName();
            }
        }
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .roleName(UserRole.getName(user.getRole()))
                .orgId(user.getOrgId())
                .orgName(orgName)
                .build();
    }
}
