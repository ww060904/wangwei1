package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("book")
@Data
@ApiModel(value = "book", description = "书籍信息")
public class BookEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 标题
     */
    @ApiModelProperty(value = "书籍标题")
    private String title;

    /**
     * 作者
     */
    @ApiModelProperty(value = "作者")
    private String author;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介")
    private String introduction;

    /**
     * 日期
     */
    @ApiModelProperty(value = "出版日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类（1:编程 2:文学 3:科技 4:历史 5:其他）")
    private Integer type;

    /**
     * 图片文件名
     */
    @ApiModelProperty(value = "封面图片文件名")
    private String image;

    /**
     * 上传时间
     */
    @ApiModelProperty(value = "上传时间")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 是否轮播图（0:否 1:是）
     */
    @ApiModelProperty(value = "是否轮播图（0:否 1:是）")
    @TableField("is_banner")
    private Integer isBanner;

    /**
     * 是否推荐（0:否 1:是）
     */
    @ApiModelProperty(value = "是否推荐（0:否 1:是）")
    @TableField("is_recommend")
    private Integer isRecommend;

    /**
     * 浏览次数
     */
    @ApiModelProperty(value = "浏览次数")
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 排序权重（数字越大越靠前）
     */
    @ApiModelProperty(value = "排序权重（数字越大越靠前）")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 图片完整访问路径（不映射数据库，返回前端使用）
     */
    @ApiModelProperty(value = "封面图片完整访问路径")
    @TableField(exist = false)
    private String imageUrl;

    @ApiModelProperty(value = "上传人id")
    private Long submitUser;

}
