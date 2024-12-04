package com.iot.BE.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity// Annotation @Entity đánh dấu lớp này là một thực thể sẽ được ánh xạ với một bảng trong cơ sở dữ liệu
@Table(name = "sensor_data") // @Table(name = "sensor_data") chỉ định tên bảng là "sensor_data" trong cơ sở dữ liệu
// Các annotation từ Lombok:
// @Data tự động sinh các phương thức getter, setter, toString, equals, và hashCode
// @AllArgsConstructor tạo constructor với tất cả các thuộc tính
// @NoArgsConstructor tạo constructor không có tham số
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorData {
    // @Id đánh dấu thuộc tính "id" là khóa chính của thực thể
    // @GeneratedValue(strategy = GenerationType.IDENTITY) thiết lập id tự động tăng
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Double temperature;
    @Column
    private Double humidity;
    @Column
    private Double light;
    // field for after request
    @Column
    private Double other;
    @Column
    private Date time;
    @Column(name = "timeconvert")
    private String timeConvert;
    //Sua
    @Column
    private Double news;
}
