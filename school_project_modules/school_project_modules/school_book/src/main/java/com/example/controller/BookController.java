package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.BookEntity;
import com.example.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/book")
@Api(tags = "图书模块")
public class BookController {

    @Autowired
    private BookService bookService;
    /**
     * 分页获取所有书籍
     */
    @ApiOperation(value = "分页获取书籍列表", notes = "分页查询所有书籍，支持按分类筛选和关键词搜索")
    @GetMapping("/list")
    public SaResult getBookList(
            @ApiParam(value = "页码，默认1") @RequestParam(defaultValue = "1", required = false) Integer pageNum,
            @ApiParam(value = "每页数量，默认10") @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            @ApiParam(value = "分类（1:编程 2:文学 3:科技 4:历史 5:其他）") @RequestParam(value = "type", required = false) Integer type,
            @ApiParam(value = "搜索关键词，可搜索书名或作者") @RequestParam(value = "keyword", required = false) String keyword) {
        return bookService.getBookPage(pageNum, pageSize, type, keyword);
    }
    /**
     * 获取推荐书籍列表
     */
    @ApiOperation(value = "获取推荐书籍列表", notes = "获取推荐书籍列表，按排序权重、时间倒序排列")
    @GetMapping("/recommend")
    public SaResult getRecommendList(
            @ApiParam(value = "限制数量，默认10")
            @RequestParam(value = "limit", required = false) Integer limit) {
        return bookService.getRecommendList(limit);
    }

    /**
     * 获取轮播图书籍列表
     */
    @ApiOperation(value = "获取轮播图书籍列表", notes = "获取用于首页轮播展示的书籍数据")
    @GetMapping("/banner")
    public SaResult getBannerList(
            @ApiParam(value = "限制数量，默认5")
            @RequestParam(value = "limit", required = false) Integer limit) {
        return bookService.getBannerList(limit);
    }

    /**
     * 获取热门书籍列表
     */
    @ApiOperation(value = "获取热门书籍列表", notes = "按浏览量从高到低查询热门书籍")
    @GetMapping("/hot")
    public SaResult getHotList(
            @ApiParam(value = "限制数量，默认10")
            @RequestParam(value = "limit", required = false) Integer limit) {
        return bookService.getHotList(limit);
    }

    /**
     * 根据ID获取书籍详情（查询时浏览量+1）
     */
    @ApiOperation(value = "根据ID获取书籍详情", notes = "查询书籍详情，并将该书浏览次数加1")
    @GetMapping("/detail")
    @Cacheable(value = "bookCache",key = "#id")
    public SaResult getBookDetail(
            @ApiParam(value = "书籍ID", required = true, example = "1") @RequestParam("id") Long id) {
        return bookService.getBookDetail(id);
    }

    @ApiOperation(
            value = "保存书籍信息",
            notes = "保存书籍到数据库。image 字段填入上传图片接口返回的 imageName；submitTime 不传则自动设为当前时间")
    @PostMapping("/save")
    public SaResult saveBook(
            @ApiParam(value = "书籍实体，image 字段填入上传图片接口返回的 imageName", required = true)
            @RequestBody BookEntity book) {
        if (book.getSubmitTime() == null) {
            book.setSubmitTime(LocalDateTime.now());
        }
        return bookService.saveBook(book);
    }


}