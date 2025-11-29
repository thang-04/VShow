package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.req.UpdateCategoryRequest;
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
public class UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;
    private final RedisService redisService;

    public CategoryResponse execute(long cateId, UpdateCategoryRequest updateCategoryRequest) {
        String prefix = "[UpdateCategoryUseCase]|";
        long start = System.currentTimeMillis();
        try{
            String key = String.format(Constant.RedisKey.REDIS_EVENT_BY_CATEGORY_ID, cateId);
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if(resultRedis != null){
                // remove cache redis
                redisService.getRedisEventTemplate().delete(key);
                log.info("{}|Deleted category cache in Redis for id {}.", prefix, cateId);
            }
            Category category = categoryRepository.findById(cateId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            if (updateCategoryRequest.getCategoryName() != null) category.setCategoryName(updateCategoryRequest.getCategoryName());
            if (updateCategoryRequest.getCategoryDescription() != null)
                category.setCategoryDescription(updateCategoryRequest.getCategoryDescription());
            Category updated = categoryRepository.save(category);
            if(updated == null){
                log.error("{}|Failed to update category in MySQL for id {}.", prefix, cateId);
                return null;
            }
            log.info("{}|Updated category success for id {}. Time taken: {} ms", prefix, cateId, (System.currentTimeMillis() - start));
            return categoryDtoMapper.toResponse(updated);
        }catch (Exception ex){
           log.error("{}|Exception|{}",prefix, ex.getMessage(), ex);
           return null;
        }
    }
}
