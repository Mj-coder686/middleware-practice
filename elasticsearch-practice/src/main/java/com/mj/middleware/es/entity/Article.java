package com.mj.middleware.es.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章实体 — 练习全文检索、高亮查询
 */
@Data
@Document(indexName = "articles")
@Setting(shards = 1, replicas = 0)
public class Article {

    @Id
    private String id;

    /** 标题 — ik_max_word 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 内容 — ik_max_word 分词 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /** 作者 */
    @Field(type = FieldType.Keyword)
    private String author;

    /** 分类 */
    @Field(type = FieldType.Keyword)
    private String category;

    /** 标签 */
    @Field(type = FieldType.Keyword)
    private String[] tags;

    /** 浏览量 */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /** 点赞数 */
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    /** 评分 */
    @Field(type = FieldType.Double)
    private Double score;

    /** 是否置顶 */
    @Field(type = FieldType.Boolean)
    private Boolean top;

    /** 发布时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime publishTime;

    /** 创建时间 */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
}
