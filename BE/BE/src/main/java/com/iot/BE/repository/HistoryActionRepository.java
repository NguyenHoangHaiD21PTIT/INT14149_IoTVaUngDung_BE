package com.iot.BE.repository;

import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryActionRepository extends JpaRepository<HistoryAction, Integer> {
    //Truy vấn theo ID
    HistoryAction findById(int id);
    //SELECT * FROM history_action WHERE id = ?;

    //Phân trang
    @Query("select data from HistoryAction data")
    List<HistoryAction> findLimited(Pageable pageable);
    //SELECT * FROM history_action LIMIT 5 OFFSET 10; lấy 5 bản ghi từ bản ghi thứ 10

    //lọc các bản ghi HistoryAction dựa trên một trong hai tiêu chí: theo tên hoặc theo ngày.
    @Query("SELECT d FROM HistoryAction d WHERE " +
            "(:field = 'name' AND d.name = :term ) OR " +
            "(:field = 'date' AND d.timeConvert = :term )")
    List<HistoryAction> filterHistoryAction(Pageable pageable, @Param("field") String field,@Param("term") String term);
    //SELECT * FROM history_action WHERE name = 'Fan On';
    // filter all field
    @Query("SELECT d FROM HistoryAction d WHERE " +
            "d.name = :term " +
            "AND d.timeConvert = :term ")
    List<HistoryAction> filterAllFieldHistoryAction(Pageable pageable,@Param("term") String term);

    //Đếm số lượng bản ghi mà quạt đã được bật trong 1 ngày nhất định
    @Query("SELECT COUNT(d) FROM HistoryAction d WHERE d.name = 'FAN' "
            + "  AND d.action = true AND STR(d.timeConvert) LIKE CONCAT(:date, '%')")
    long countTrueStatusForFanToday(@Param("date") String date);
    //SELECT COUNT(*)
    //FROM history_action
    //WHERE name = 'FAN'
    //AND action = true
    //AND timeconvert LIKE '2024-11-13%';
}
