package com.iot.BE.entity;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@Data //Tự tạo get set
@AllArgsConstructor //Hàm khởi tạo đầy đủ các tham số
@NoArgsConstructor //Hàm khởi tạo không chứa tham số
public class Device {
    @Id //khoá chính
    //Tự động tăng
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private String name;
    @Column
    private Boolean status;
    //Sua
}
