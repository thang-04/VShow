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

@Slf4j
@Service
@RequiredArgsConstructor
public class ListCategoryUseCase {

    private static final long CATEGORY_TTL_HOURS = 1L;
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final RedisService redisService;

    public List<CategoryResponse> getAllCategories() {
        String prefix = "[ListCategoryUseCase]|";
        long start = System.currentTimeMillis();
        List<Category> listCategories = new ArrayList<>();
        try {
            String key = Constant.RedisKey.REDIS_LIST_CATEGORY;
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                // get by MYSQL
                List<Category> list = categoryRepository.findAll();
                log.info("{}|Fetched categories from MySQL: {} categories found.", prefix, list.size());
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
                    redisService.getRedisEventTemplate().expire(key, CATEGORY_TTL_HOURS, TimeUnit.HOURS);
                    log.info("{}|Stored categories in Redis cache.", prefix);
                }
            } else {
                listCategories = (List<Category>) gson.fromJson(resultRedis, new TypeToken<List<Category>>() {
                }.getType());
                log.info("{}|Fetched categories from Redis cache: {} categories found.", prefix, listCategories.size());
            }
            log.info("{}|Time taken: {} ms", prefix, (System.currentTimeMillis() - start));
            return categoryDtoMapper.toResponseList(listCategories);
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
            return List.of();
        }
    }
}
