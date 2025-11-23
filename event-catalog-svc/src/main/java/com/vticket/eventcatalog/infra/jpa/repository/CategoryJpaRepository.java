package com.vticket.eventcatalog.infra.jpa.repository;

import com.vticket.eventcatalog.infra.jpa.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

}
