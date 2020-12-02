package com.wcy.rhapsody.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.mail.MailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcy.rhapsody.admin.config.redis.RedisService;
import com.wcy.rhapsody.admin.mapper.ColumnMapper;
import com.wcy.rhapsody.admin.mapper.UserMapper;
import com.wcy.rhapsody.admin.model.dto.RegisterDTO;
import com.wcy.rhapsody.admin.model.entity.Column;
import com.wcy.rhapsody.admin.model.entity.Follow;
import com.wcy.rhapsody.admin.model.entity.Topic;
import com.wcy.rhapsody.admin.model.entity.User;
import com.wcy.rhapsody.admin.model.vo.ProfileVO;
import com.wcy.rhapsody.admin.service.FollowService;
import com.wcy.rhapsody.admin.service.TopicService;
import com.wcy.rhapsody.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 用户 实现类
 *
 * @author Yeeep 2020/11/7
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private FollowService followService;

    @Resource
    private TopicService topicService;

    @Resource
    private RedisService redisService;

    @Resource
    private ColumnMapper columnMapper;

    @Value("${config.domain}")
    private String domain;

    @Override
    public User selectByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public void createUser(RegisterDTO dto) {
        User user = User.builder()
                .username(dto.getName())
                .alias(dto.getName())
                .password(BCrypt.hashpw(dto.getPass(), BCrypt.gensalt()))
                .email(dto.getEmail())
                .createTime(new Date())
                .build();
        this.baseMapper.insert(user);
        String activeCode = RandomUtil.randomString(8);
        // 30分钟
        redisService.set("activeCode[" + dto.getName() + "]", activeCode, 30 * 60);

        // 发送邮件
        String activeUrl = URLUtil.normalize(domain + "?user=" + dto.getName() + "&code=" + activeCode);
        String content = "请在30分钟内激活您的账号，如非本人操作，请忽略 </br > " +
                "<a href=\"" + activeUrl + "\" target =\"_blank\" '>点击激活账号</a>";
        MailUtil.send(dto.getEmail(), "【滚雪球】账号激活", content, true);
    }


    @Override
    public ProfileVO getUserProfile(String id) {
        ProfileVO profile = new ProfileVO();
        User user = this.baseMapper.selectById(id);
        BeanUtils.copyProperties(user, profile);
        // 用户文章数
        int count = topicService.count(new LambdaQueryWrapper<Topic>().eq(Topic::getUserId, id));
        profile.setTopicCount(count);

        // 粉丝数
        int followers = followService.count(new LambdaQueryWrapper<Follow>().eq(Follow::getParentId, id));
        profile.setFollowerCount(followers);
        // 关注数
        int follows = followService.count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, id));
        profile.setFollowCount(follows);
        // 专栏数
        Integer integer = columnMapper.selectCount(new LambdaQueryWrapper<Column>().eq(Column::getUserId, id));
        profile.setColumns(integer);

        return profile;
    }
}