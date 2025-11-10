package com.moveo.ha.repository;

import com.moveo.ha.entity.Task;
import com.moveo.ha.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProject_Id(Long projectId, Pageable pageable);

    Page<Task> findByProject_IdAndStatus(Long projectId, TaskStatus status, Pageable pageable);

    boolean existsByIdAndProject_Id(Long taskId, Long projectId);
}
