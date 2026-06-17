package com.example.service.Impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.BookEntity;
import com.example.mapper.BookMapper;
import com.example.service.BookService;
import com.example.service.UserService;
import com.example.utils.FileUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, BookEntity> implements BookService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserService userService;
    /**
     * 分页获取所有书籍
     */
    @Override
    public SaResult getBookPage(Integer pageNum, Integer pageSize, Integer type, String keyword) {
        Page<BookEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type != null, BookEntity::getType, type)
                .and(keyword != null && !keyword.trim().isEmpty(), wrapper ->
                        wrapper.like(BookEntity::getTitle, keyword).or().like(BookEntity::getAuthor, keyword))
                .isNotNull(BookEntity::getTitle)
                .orderByDesc(BookEntity::getSortOrder)
                .orderByDesc(BookEntity::getSubmitTime);

        Page<BookEntity> resultPage = bookMapper.selectPage(page, queryWrapper);

        // 填充图片路径
        List<BookEntity> records = fillImageUrl(resultPage.getRecords());
        resultPage.setRecords(records);

        return SaResult.data(resultPage);
    }

    /**
     * 获取轮播图书籍列表
     */
    @Override
    public SaResult getBannerList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5;
        }
        List<BookEntity> books = bookMapper.selectBannerBooks(limit);
        return SaResult.data(fillImageUrl(books));
    }

    /**
     * 获取热门书籍列表
     */
    @Override
    public SaResult getHotList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<BookEntity> books = bookMapper.selectHotBooks(limit);
        return SaResult.data(fillImageUrl(books));
    }

    /**
     * 获取推荐书籍列表
     */
    @Override
    public SaResult getRecommendList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<BookEntity> books = bookMapper.selectRecommendBooks(limit);
        return SaResult.data(fillImageUrl(books));
    }

    /**
     * 根据ID查询书籍详情（查询时浏览量 +1）
     */
    @Override
    public SaResult getBookDetail(Long id) {
        if (id == null) {
            return SaResult.error("id不能为空");
        }

        int rows = bookMapper.increaseViewCount(id);
        if (rows <= 0) {
            return SaResult.error("未查询到该书籍");
        }

        BookEntity book = bookMapper.selectById(id);
        if (book == null) {
            return SaResult.error("未查询到该书籍");
        }
        book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()));
        return SaResult.data(book);
    }

    @Override
    public SaResult saveBook(BookEntity book) {
        // 自动补充上传时间
        if (book.getSubmitTime() == null) {
            book.setSubmitTime(LocalDateTime.now());
        }

        // 获取用户的id - 使用 userService
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId == null) {
                return SaResult.error("用户未登录");
            }

            Long userId = userService.selectByPhone(loginId.toString());
            if (userId == null) {
                return SaResult.error("未找到用户信息");
            }

            book.setSubmitUser(userId);
            boolean success = save(book);
            return success ? SaResult.ok("保存成功") : SaResult.error("保存失败");
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 填充图片完整访问路径
     * @param books 书籍列表
     * @return 填充后的书籍列表
     */
    private List<BookEntity> fillImageUrl(List<BookEntity> books) {
        if (books == null || books.isEmpty()) {
            return books;
        }
        return books.stream().peek(book ->
                book.setImageUrl(FileUrlUtils.getImageUrl(book.getImage()))
        ).collect(Collectors.toList());
    }

    @Override
    public List<String> getImagesNames() {
        List<String> list = new ArrayList<>();
        List<BookEntity> books = bookMapper.selectList(null);
        for (BookEntity book : books) {
            list.add(book.getImage());
        }
        return list;
    }
}