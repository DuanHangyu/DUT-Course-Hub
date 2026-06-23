-- 班级架构管理数据库迁移脚本
-- 执行顺序: V1 -> V2

-- V1: 创建班级表 school_class
CREATE TABLE IF NOT EXISTS `school_class` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `school_id` BIGINT NOT NULL COMMENT '学校ID',
    `class_name` VARCHAR(50) NOT NULL COMMENT '班级名称',
    `teacher_id` BIGINT COMMENT '班主任ID，关联user表',
    `student_count` INT DEFAULT 0 COMMENT '学生人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_school_id` (`school_id`),
    INDEX `idx_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- V2: 修改 student 表，添加 school_id 和 class_id 字段
-- 注意：执行前请确认 student 表中 schoolClass 字段的数据迁移方案
ALTER TABLE `student` ADD COLUMN `school_id` BIGINT COMMENT '学校ID' AFTER `id`;
ALTER TABLE `student` ADD COLUMN `class_id` BIGINT COMMENT '班级ID，关联school_class表' AFTER `school_id`;
ALTER TABLE `student` ADD INDEX `idx_school_id` (`school_id`);
ALTER TABLE `student` ADD INDEX `idx_class_id` (`class_id`);
