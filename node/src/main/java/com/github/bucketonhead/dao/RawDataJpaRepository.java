package com.github.bucketonhead.dao;

import com.github.bucketonhead.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataJpaRepository extends JpaRepository<RawData, Long> {
}
