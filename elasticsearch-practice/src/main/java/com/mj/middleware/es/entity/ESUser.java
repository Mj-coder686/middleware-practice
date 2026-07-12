package com.mj.middleware.es.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

/**
 * ES 用户实体 — 练习精确匹配、范围查询
 */
@Data
@Document(indexName = "es_users")
@Setting(shards = 1, replicas = 0)
public class ESUser {

    @Id
    private String id;

    /** 用户名 — keyword 精确匹配 */
    @Field(type = FieldType.Keyword)
    private String username;

    /** 昵称 — text 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String nickname;

    /** 邮箱 */
    @Field(type = FieldType.Keyword)
    private String email;

    /** 年龄 */
    @Field(type = FieldType.Integer)
    private Integer age;

    /** 城市 */
    @Field(type = FieldType.Keyword)
    private String city;

    /** 状态: 0-禁用, 1-正常 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 薪资 — 范围查询用 */
    @Field(type = FieldType.Double)
    private Double salary;

    /** 注册时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
}
