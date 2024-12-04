package com.iot.BE.repository;

import com.iot.BE.entity.SensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Integer> {

    //Phân trang
    @Query("select  data from SensorData data")
    List<SensorData> findLimited(Pageable pageable);

    //  filter 1 field
    //Sửa them quẻy
    @Query("SELECT d FROM SensorData d WHERE " +
            "(:field = 'temperature' AND STR(d.temperature) = :term ) OR " +
            "(:field = 'humidity' AND STR(d.humidity) = :term ) OR " +
            "(:field = 'light' AND STR(d.light) = :term ) OR " +
            "(:field = 'date' AND STR(d.timeConvert) = :term )" +
            "(:field = 'news' AND STR(d.news) = :term )" )
    List<SensorData> filterSensorData(Pageable pageable,@Param("field") String field,@Param("term") String term);
    //SELECT * FROM sensor_data WHERE temperature = 25.5;

    // filter all fields
    @Query("SELECT d FROM SensorData d WHERE " +
            "STR(d.temperature) = :term " +
            "AND STR(d.humidity) = :term " +
            "AND STR(d.light) = :term " +
            "AND STR( d.timeConvert) = :term " +
            "AND STR (d.news) = :term")
    List<SensorData> filterAllFieldSensorData(Pageable pageable,@Param("term") String term);
    //SELECT * FROM sensor_data
    //WHERE temperature = 25.5
    //AND humidity = 60
    //AND light = 100
    //AND timeconvert = '2024-11-13';

    // count other pass over 80
    @Query("SELECT COUNT(d) FROM SensorData d WHERE d.other >= 80 AND STR(d.timeConvert) LIKE CONCAT(:date, '%')")
    long countWindyGreaterThan80(@Param("date") String date);
    //SELECT COUNT(*) FROM sensor_data
    //WHERE other >= 80 AND timeconvert LIKE '2024-11-13%';
}
