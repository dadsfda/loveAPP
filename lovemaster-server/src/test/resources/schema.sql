CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    gender TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    pair_code VARCHAR(20),
    partner_id BIGINT,
    paired_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_phone UNIQUE (phone),
    CONSTRAINT uk_email UNIQUE (email),
    CONSTRAINT uk_pair_code UNIQUE (pair_code)
);

CREATE TABLE IF NOT EXISTS couple (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    status TINYINT DEFAULT 1 NOT NULL,
    paired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_couple_user_id_1 ON couple(user_id_1);
CREATE INDEX IF NOT EXISTS idx_couple_user_id_2 ON couple(user_id_2);
CREATE INDEX IF NOT EXISTS idx_couple_status ON couple(status);

CREATE TABLE IF NOT EXISTS anniversary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    couple_id BIGINT,
    title VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(30) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    remind_days VARCHAR(50),
    surprise_mode TINYINT DEFAULT 0 NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_anniversary_creator_id ON anniversary(creator_id);
CREATE INDEX IF NOT EXISTS idx_anniversary_couple_id ON anniversary(couple_id);
CREATE INDEX IF NOT EXISTS idx_anniversary_date ON anniversary(date);
CREATE INDEX IF NOT EXISTS idx_anniversary_visibility ON anniversary(visibility);

CREATE TABLE IF NOT EXISTS preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    couple_id BIGINT,
    target VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    content VARCHAR(500) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    tags VARCHAR(255),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_preference_creator_id ON preference(creator_id);
CREATE INDEX IF NOT EXISTS idx_preference_couple_id ON preference(couple_id);
CREATE INDEX IF NOT EXISTS idx_preference_visibility ON preference(visibility);
CREATE INDEX IF NOT EXISTS idx_preference_category ON preference(category);

CREATE TABLE IF NOT EXISTS date_idea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(80) NOT NULL,
    budget_level VARCHAR(30) NOT NULL,
    duration_level VARCHAR(30) NOT NULL,
    scene VARCHAR(30) NOT NULL,
    interest_tags VARCHAR(255) NOT NULL,
    steps VARCHAR(1000) NOT NULL,
    tips VARCHAR(500),
    enabled TINYINT DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_date_idea_budget ON date_idea(budget_level);
CREATE INDEX IF NOT EXISTS idx_date_idea_duration ON date_idea(duration_level);
CREATE INDEX IF NOT EXISTS idx_date_idea_scene ON date_idea(scene);
CREATE INDEX IF NOT EXISTS idx_date_idea_enabled ON date_idea(enabled);

DELETE FROM date_idea WHERE id BETWEEN 1 AND 20;

INSERT INTO date_idea
(id, title, budget_level, duration_level, scene, interest_tags, steps, tips, enabled)
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

CREATE TABLE IF NOT EXISTS memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    couple_id BIGINT,
    title VARCHAR(80) NOT NULL,
    memory_date DATE NOT NULL,
    location VARCHAR(120),
    content VARCHAR(2000),
    image_url VARCHAR(500),
    visibility VARCHAR(20) NOT NULL,
    tags VARCHAR(255),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_memory_creator_id ON memory(creator_id);
CREATE INDEX IF NOT EXISTS idx_memory_couple_id ON memory(couple_id);
CREATE INDEX IF NOT EXISTS idx_memory_visibility ON memory(visibility);
CREATE INDEX IF NOT EXISTS idx_memory_memory_date ON memory(memory_date);
