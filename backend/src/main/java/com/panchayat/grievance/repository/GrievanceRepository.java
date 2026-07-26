package com.panchayat.grievance.repository;

import com.panchayat.grievance.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    boolean existsByGrievanceId(String grievanceId);

    /**
     * Task 3: search across the visible fields + optional status/category filter.
     * Everything is ordered so unresolved cases come first, oldest date_raised first -
     * i.e. whatever needs attention appears at the top of the list.
     */
    @Query("SELECT g FROM Grievance g WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "  LOWER(g.complainant) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(g.grievanceId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR g.status = :status) " +
           "AND (:category IS NULL OR :category = '' OR g.category = :category) " +
           "AND (:department IS NULL OR :department = '' OR g.department = :department) " +
           "ORDER BY " +
           "  CASE g.status WHEN 'Open' THEN 0 WHEN 'In Progress' THEN 1 ELSE 2 END, " +
           "  g.dateRaised ASC")
    List<Grievance> searchAndFilter(@Param("search") String search,
                                     @Param("status") String status,
                                     @Param("category") String category,
                                     @Param("department") String department);
}
