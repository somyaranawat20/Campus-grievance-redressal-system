package com.inn.cgrs.repository;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceCategory;
import com.inn.cgrs.model.GrievanceStatus;
import com.inn.cgrs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {
    List<Grievance> findByUser(User user);
    List<Grievance> findByUserOrderByCreatedAtDesc(User user);
    List<Grievance> findByStatus(GrievanceStatus status);
    List<Grievance> findByCategory(GrievanceCategory category);
    List<Grievance> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT g FROM Grievance g WHERE g.user = :user AND g.status = :status ORDER BY g.createdAt DESC")
    List<Grievance> findByUserAndStatus(@Param("user") User user, @Param("status") GrievanceStatus status);
    
    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.status = :status")
    long countByStatus(@Param("status") GrievanceStatus status);

    List<Grievance> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<Grievance> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(GrievanceStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.status = :status AND g.createdAt BETWEEN :start AND :end")
    long countByStatusAndCreatedAtBetween(@Param("status") GrievanceStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Grievance> findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(Collection<GrievanceStatus> statuses, LocalDateTime before);
}
