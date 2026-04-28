-- 创建数据库
CREATE DATABASE IF NOT EXISTS lovemaster DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lovemaster;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `pair_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
    `partner_id` BIGINT DEFAULT NULL COMMENT '伴侣ID',
    `paired_at` DATETIME DEFAULT NULL COMMENT '配对时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_pair_code` (`pair_code`),
    KEY `idx_partner_id` (`partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 情侣关系表
CREATE TABLE IF NOT EXISTS `couple` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '情侣关系ID',
    `user_id_1` BIGINT NOT NULL COMMENT '用户1 ID',
    `user_id_2` BIGINT NOT NULL COMMENT '用户2 ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-解除 1-有效',
    `paired_at` DATETIME NOT NULL COMMENT '配对时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_1` (`user_id_1`),
    KEY `idx_user_id_2` (`user_id_2`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣关系表';

-- 纪念日表
CREATE TABLE IF NOT EXISTS `anniversary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '纪念日ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `couple_id` BIGINT DEFAULT NULL COMMENT '情侣关系ID',
    `title` VARCHAR(50) NOT NULL COMMENT '标题',
    `date` DATE NOT NULL COMMENT '纪念日期',
    `type` VARCHAR(30) NOT NULL COMMENT '类型：LOVE_ANNIVERSARY/BIRTHDAY/CUSTOM',
    `visibility` VARCHAR(20) NOT NULL COMMENT '可见性：PRIVATE/COUPLE',
    `remind_days` VARCHAR(50) DEFAULT NULL COMMENT '提醒提前天数，逗号分隔',
    `surprise_mode` TINYINT NOT NULL DEFAULT 0 COMMENT '惊喜模式：0-否 1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_couple_id` (`couple_id`),
    KEY `idx_date` (`date`),
    KEY `idx_visibility` (`visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纪念日表';

-- 喜好清单表
CREATE TABLE IF NOT EXISTS `preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '喜好记录ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `couple_id` BIGINT DEFAULT NULL COMMENT '情侣关系ID',
    `target` VARCHAR(30) NOT NULL COMMENT '记录对象：SELF/PARTNER_OBSERVED',
    `category` VARCHAR(30) NOT NULL COMMENT '分类',
    `content` VARCHAR(500) NOT NULL COMMENT '内容',
    `visibility` VARCHAR(20) NOT NULL COMMENT '可见性：PRIVATE/COUPLE',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_preference_creator_id` (`creator_id`),
    KEY `idx_preference_couple_id` (`couple_id`),
    KEY `idx_preference_visibility` (`visibility`),
    KEY `idx_preference_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喜好清单表';

-- 约会灵感模板表
CREATE TABLE IF NOT EXISTS `date_idea` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '约会灵感ID',
    `title` VARCHAR(80) NOT NULL COMMENT '标题',
    `budget_level` VARCHAR(30) NOT NULL COMMENT '预算档位',
    `duration_level` VARCHAR(30) NOT NULL COMMENT '耗时档位',
    `scene` VARCHAR(30) NOT NULL COMMENT '场景',
    `interest_tags` VARCHAR(255) NOT NULL COMMENT '兴趣标签，逗号分隔',
    `steps` VARCHAR(1000) NOT NULL COMMENT '执行步骤',
    `tips` VARCHAR(500) DEFAULT NULL COMMENT '小提示',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_date_idea_budget` (`budget_level`),
    KEY `idx_date_idea_duration` (`duration_level`),
    KEY `idx_date_idea_scene` (`scene`),
    KEY `idx_date_idea_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='约会灵感模板表';

DELETE FROM `date_idea` WHERE `id` BETWEEN 1 AND 20;

INSERT INTO `date_idea`
(`id`, `title`, `budget_level`, `duration_level`, `scene`, `interest_tags`, `steps`, `tips`, `enabled`)
VALUES
(1, '傍晚散步拍照', 'FREE', 'ONE_HOUR', 'OUTDOOR', '散步,拍照', '选一条熟悉路线;各自拍三张喜欢的画面;结束后互相选一张收藏', '适合低成本恢复连接感', 1),
(2, '居家电影夜', 'UNDER_50', 'TWO_HOURS', 'HOME', '电影,零食', '提前选两部候选电影;准备饮料零食;看完互相说一个喜欢的片段', '不要临时争选片，先定候选池', 1),
(3, '雨天咖啡聊天', 'UNDER_100', 'TWO_HOURS', 'RAINY_DAY', '咖啡,聊天', '选择安静咖啡店;各自带一个近况话题;最后一起定下周小计划', '适合不想奔波的雨天', 1),
(4, '公园野餐', 'UNDER_100', 'HALF_DAY', 'OUTDOOR', '美食,散步,拍照', '准备简单食物;找一片草地;饭后散步拍照', '注意天气和垃圾带走', 1),
(5, '一起做晚饭', 'UNDER_100', 'TWO_HOURS', 'HOME', '美食,手作', '一起选一道菜;分工采购和处理;吃饭时互夸一个环节', '避免复杂菜式，优先成功率', 1),
(6, '书店交换推荐', 'UNDER_50', 'ONE_HOUR', 'INDOOR', '阅读,聊天', '各自挑一本想推荐给对方的书;说明推荐理由;拍下书名以后再读', '不一定要购买', 1),
(7, '手作小物', 'UNDER_100', 'HALF_DAY', 'INDOOR', '手作,礼物', '选择简单手作材料;一起完成;写下赠送小卡片', '成品不完美也可以保留为回忆', 1),
(8, '城市漫游', 'UNDER_50', 'HALF_DAY', 'OUTDOOR', '散步,拍照,探索', '选一个没认真逛过的街区;随机进一家小店;记录三个新发现', '适合周末半天', 1),
(9, '线上同步观影', 'UNDER_50', 'TWO_HOURS', 'REMOTE', '电影,异地', '约定开始时间;语音同步观看;结束后分享三句话感受', '异地时保持轻量陪伴', 1),
(10, '一起运动拉伸', 'FREE', 'ONE_HOUR', 'HOME', '运动,健康', '选 20 分钟低强度视频;一起完成;结束后喝水休息', '不要互相评价动作标准', 1),
(11, '博物馆半日', 'UNDER_100', 'HALF_DAY', 'INDOOR', '展览,学习,拍照', '提前预约;各自选一个最喜欢展品;结束后吃简餐', '适合安静型约会', 1),
(12, '夜市小吃挑战', 'UNDER_100', 'TWO_HOURS', 'OUTDOOR', '美食,探索', '每人选两样小吃;共同打分;最后选今晚第一名', '注意饮食禁忌', 1),
(13, '共同歌单整理', 'FREE', 'ONE_HOUR', 'HOME', '音乐,回忆', '各自选五首歌;讲一个相关回忆;生成共同歌单', '适合睡前轻互动', 1),
(14, '拼图或桌游夜', 'UNDER_100', 'TWO_HOURS', 'HOME', '桌游,陪伴', '选择轻规则游戏;准备饮料;玩后复盘最好笑瞬间', '避免胜负压力太强的游戏', 1),
(15, '短途骑行', 'UNDER_50', 'HALF_DAY', 'OUTDOOR', '运动,探索', '选安全路线;中途休息拍照;结束后补水', '安全优先，天气不好就取消', 1),
(16, '共同整理相册', 'FREE', 'ONE_HOUR', 'HOME', '回忆,拍照', '各自选十张照片;按时间排序;挑三张做纪念日素材', '适合为回忆功能预热', 1),
(17, '早餐约会', 'UNDER_50', 'ONE_HOUR', 'OUTDOOR', '美食,日常', '约一家早餐店;吃完散步十分钟;互相说当天期待', '适合工作日前的小仪式', 1),
(18, '学习陪伴局', 'FREE', 'TWO_HOURS', 'INDOOR', '学习,陪伴', '各自定一个学习目标;番茄钟 2 轮;结束后分享成果', '适合需要自律但想陪伴时', 1),
(19, '礼物灵感清单', 'FREE', 'ONE_HOUR', 'HOME', '礼物,聊天', '各自写五个想收到的小东西;标预算;保存到喜好清单', '不要变成索要礼物', 1),
(20, '异地明信片计划', 'UNDER_50', 'TWO_HOURS', 'REMOTE', '异地,礼物,文字', '各自写一段想说的话;约定寄出日期;收到后拍照记录', '适合异地情侣制造期待', 1);

-- 回忆收藏夹表
CREATE TABLE IF NOT EXISTS `memory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回忆ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `couple_id` BIGINT DEFAULT NULL COMMENT '情侣关系ID',
    `title` VARCHAR(80) NOT NULL COMMENT '标题',
    `memory_date` DATE NOT NULL COMMENT '回忆发生日期',
    `location` VARCHAR(120) DEFAULT NULL COMMENT '地点文本',
    `content` VARCHAR(2000) DEFAULT NULL COMMENT '正文',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
    `visibility` VARCHAR(20) NOT NULL COMMENT '可见性：PRIVATE/COUPLE',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_memory_creator_id` (`creator_id`),
    KEY `idx_memory_couple_id` (`couple_id`),
    KEY `idx_memory_visibility` (`visibility`),
    KEY `idx_memory_memory_date` (`memory_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回忆收藏夹表';
