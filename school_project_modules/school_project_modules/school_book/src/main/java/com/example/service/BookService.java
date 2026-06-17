package com.example.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.BookEntity;

import java.util.List;

public interface BookService extends IService<BookEntity> {
    SaResult getBookPage(Integer pageNum, Integer pageSize, Integer type, String keyword);
    SaResult getBannerList(Integer limit);
    SaResult getHotList(Integer limit);
    SaResult getRecommendList(Integer limit);
    SaResult getBookDetail(Long id);

    SaResult saveBook(BookEntity book);

    List<String> getImagesNames();
}