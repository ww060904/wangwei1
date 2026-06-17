package com.example.mapper;

import com.example.entity.BookEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 书籍数据访问层
 */
@Mapper
@Repository
public interface BookMapper extends BaseMapper<BookEntity> {

    /**
     * 查询轮播图书籍
     * @param limit 限制条数
     * @return 书籍列表
     */
    @Select("SELECT * FROM book WHERE is_banner = 1 ORDER BY sort_order DESC, submit_time DESC LIMIT #{limit}")
    List<BookEntity> selectBannerBooks(@Param("limit") Integer limit);

    /**
     * 查询热门书籍（按浏览量倒序）
     * @param limit 限制条数
     * @return 书籍列表
     */
    @Select("SELECT * FROM book ORDER BY view_count DESC, sort_order DESC, submit_time DESC LIMIT #{limit}")
    List<BookEntity> selectHotBooks(@Param("limit") Integer limit);

    /**
     * 查询推荐书籍
     * @param limit 限制条数
     * @return 书籍列表
     */
    @Select("SELECT * FROM book WHERE is_recommend = 1 ORDER BY sort_order DESC, submit_time DESC LIMIT #{limit}")
    List<BookEntity> selectRecommendBooks(@Param("limit") Integer limit);

    /**
     * 浏览量 +1
     * @param id 书籍ID
     * @return 影响行数
     */
    @Update("UPDATE book SET view_count = IFNULL(view_count, 0) + 1 WHERE id = #{id}")
    int increaseViewCount(@Param("id") Long id);

}