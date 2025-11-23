package com.vticket.eventcatalog.web;

import com.google.gson.Gson;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.eventcatalog.app.dto.req.CreateCategoryRequest;
import com.vticket.eventcatalog.app.dto.req.UpdateCategoryRequest;
import com.vticket.eventcatalog.app.dto.res.CategoryResponse;
import com.vticket.eventcatalog.app.usercase.CreateCategoryUseCase;
import com.vticket.eventcatalog.app.usercase.ListCategoryUseCase;
import com.vticket.eventcatalog.app.usercase.UpdateCategoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events/category")
@RequiredArgsConstructor
public class CategoryController {

    private final Gson gson;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final ListCategoryUseCase listCategoryUseCase;

    @GetMapping
    public String findAllCategories() {
        String prefix = "[findAllCategories]";
        try {
            List<CategoryResponse> listCategory = listCategoryUseCase.execute();
            if (listCategory.isEmpty()) {
                log.error("{}|ListCategory is empty", prefix);
                return ResponseJson.of(ErrorCode.CATEGORY_NOT_FOUND, "List of Categories is empty");
            } else {
                log.info("{}|ListCategory is {}", prefix, gson.toJson(listCategory));
                return ResponseJson.success("List of Categories found", listCategory);
            }
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, e.getMessage());
        }
    }

    @PostMapping
    public String createCategory(@RequestBody CreateCategoryRequest request) {
        String prefix = "[createCategory]";
        log.info("{}|Request: {}", prefix, gson.toJson(request));
        try {
            CategoryResponse categoryResponse = createCategoryUseCase.execute(request);
            if (categoryResponse == null) {
                log.error("{}|CategoryResponse is null", prefix);
                return ResponseJson.of(ErrorCode.CATEGORY_NOT_FOUND, "CategoryResponse is null");
            } else {
                log.info("{}|CategoryResponse is {}", prefix, gson.toJson(categoryResponse));
                return ResponseJson.success("CategoryResponse is created", categoryResponse);
            }
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, e.getMessage());
        }
    }

    @PutMapping("/update")
    public String updateCategory(@RequestParam Long id, @RequestBody UpdateCategoryRequest request) {
        String prefix = "[updateCategory]";
        log.info("{}|Request: {}", prefix, gson.toJson(request));
        try {
            CategoryResponse categoryResponse = updateCategoryUseCase.execute(id, request);
            if (categoryResponse == null) {
                log.error("{}|CategoryResponse is null", prefix);
                return ResponseJson.of(ErrorCode.CATEGORY_NOT_FOUND, "CategoryResponse is null");
            } else {
                log.info("{}|CategoryResponse is {}", prefix, gson.toJson(categoryResponse));
                return ResponseJson.success("CategoryResponse is updated successfully", categoryResponse);
            }
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, e.getMessage());
        }
    }
}
