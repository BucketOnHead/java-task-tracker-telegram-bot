package com.github.bucketonhead.dao;

import com.github.bucketonhead.entity.task.AppTask;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppTaskJpaRepository extends JpaRepository<AppTask, Long> {
    List<AppTask> findAllByCreatorId(Long creatorId, Sort sort);
}
