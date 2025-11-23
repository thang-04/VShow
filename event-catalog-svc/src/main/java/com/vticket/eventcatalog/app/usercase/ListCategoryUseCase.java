package com.vticket.eventcatalog.app.usercase;

import com.google.gson.reflect.TypeToken;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.app.mapper.CategoryDtoMapper;
import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final RedisService redisService;

    public List<CategoryResponse> execute() {
        long start = System.currentTimeMillis();
        List<Category> listCategories = new ArrayList<>();
        try {
            String key = Constant.RedisKey.REDIS_LIST_CATEGORY;
            String resultRedis = (String) redisService.getRedisEventTemplate().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                // get by MYSQL
                List<Category> list = categoryRepository.findAll();
                log.info("Fetched categories from MySQL: {} categories found.", list.size());
                Category cate;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Category category : list) {
                        cate = new Category();
                        cate.setId(category.getId());
                        cate.setCategoryName(category.getCategoryName());
                        cate.setCategoryDescription(category.getCategoryDescription());
                        listCategories.add(cate);
                    }
                }
                // cache redis
                if (CollectionUtils.isNotEmpty(listCategories)) {
                    redisService.getRedisEventTemplate().opsForValue().set(key, gson.toJson(listCategories));
                    redisService.getRedisEventTemplate().expire(key, 1, TimeUnit.HOURS);
                    log.info("Stored categories in Redis cache.");
                }
            } else {
                listCategories = (List<Category>) gson.fromJson(resultRedis, new TypeToken<List<Category>>() {
                }.getType());
                log.info("Fetched categories from Redis cache: {} categories found.", listCategories.size());
            }
            log.info("getAllCategories|Time taken: {} ms", (System.currentTimeMillis() - start));
            return categoryDtoMapper.toResponseList(listCategories);
        } catch (Exception ex) {
            log.error("getListSeat|Exception|{}", ex.getMessage(), ex);
            return List.of();
        }
    }
}
