package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.req.CreateCategoryRequest;
import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.app.mapper.CategoryDtoMapper;
import com.vticket.eventcatalog.domain.entity.Category;
import com.vticket.eventcatalog.domain.repository.CategoryRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final RedisService redisService;

    public CategoryResponse execute(CreateCategoryRequest request) {
        String prefix = "[CreateCategoryUseCase]|";
        long start = System.currentTimeMillis();
        try {
            String key = Constant.RedisKey.REDIS_LIST_CATEGORY;
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if(resultRedis != null){
                // remove cache redis
                redisService.getRedisEventTemplate().delete(key);
                log.info("{}|Deleted category list cache in Redis.", prefix);
            }
            Category category = Category.builder()
                    .categoryName(request.getCategoryName())
                    .categoryDescription(request.getCategoryDescription())
                    .build();
            Category saved = categoryRepository.save(category);
            if (saved == null) {
                log.error("{}|Failed to create category in MySQL.", prefix);
                return null;
            }
            log.info("{}|Created category success. Time taken: {} ms", prefix, (System.currentTimeMillis() - start));
            return categoryDtoMapper.toResponse(saved);
        } catch (Exception ex) {
            log.error("{}|Exception while clearing category list cache|{}", prefix, ex.getMessage(), ex);
            return null;
        }
    }
}
