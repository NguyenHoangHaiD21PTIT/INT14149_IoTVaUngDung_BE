package com.iot.BE.api;

import com.iot.BE.entity.Device;
import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import com.iot.BE.repository.DeviceRepository;
import com.iot.BE.repository.HistoryActionRepository;
import com.iot.BE.repository.SensorDataRepository;
import com.iot.BE.service.MosquittoService;
import com.iot.BE.utils.Constant;
import com.iot.BE.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CrossOrigin(origins = "http://192.168.224.195:3000")
@Repository
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private SensorDataRepository sensorDataRepository;
    @Autowired
    private HistoryActionRepository actionRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private MosquittoService mosquittoService;
    @Autowired
    private HistoryActionRepository historyActionRepository;

    @GetMapping("/")
    // API endpoint cho trang chủ, trả về danh sách SensorData được phân trang
    public ResponseEntity<List<SensorData>> home() {
        // Set default pagination
        // page 0, size 20, sort DESC field date
        Pageable pageable = PageRequest.of(0,20, Sort.by(Sort.Direction.DESC,"time"));
        // get data pagination
        List<SensorData> ans = sensorDataRepository.findLimited(pageable);
        return ResponseEntity.ok(ans);
    }

    // API endpoint trả về danh sách tất cả các thiết bị
    @GetMapping("/alldevice")
    public ResponseEntity<List<Device>> alldevice() {
        List<Device> ans = deviceRepository.findAll();
        return ResponseEntity.ok(ans);
    }

    //Lấy dữ liệu từ bảng cảm biến theo thời gian thực
    @GetMapping("/sensordata")
    public ResponseEntity<List<SensorData>> sensordata(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "term", required = false) String term) { //giá trị tìm kiếm
        // Tạo đối tượng Pageable với phân trang mặc định (mặc định là sắp xếp theo "time" giảm dần)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"time"));
        List<SensorData> ans = new ArrayList<>();
        //Hỗ trợ tìm kiếm theo trường, theo giá trị, có sắp xếp
        /*
         * Nếu field là null (không truyền tham số field), không cần lọc theo trường,
         * mặc định sẽ lấy tất cả dữ liệu với phân trang và sắp xếp theo "time"
         */
        if(field == null){
            pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "time"));
        }
        /*
        Nếu field = "all", nghĩa là muốn lọc tất cả các trường, gọi phương thức filterAllFieldSensorData.
        * Trả về kết quả sau khi lọc
         */
        else if(field.equals("all")){
            ans = sensorDataRepository.filterAllFieldSensorData(pageable,term);
            return ResponseEntity.ok(ans);
        }
        // Nếu term là null hoặc rỗng, không có giá trị tìm kiếm
        if(term == null || term.isEmpty() ){
            if(field == null || field.isEmpty()){
                // set pageable
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "time"));
                // Nếu có trường lọc, sắp xếp theo trường đó (theo "DESC" hoặc "ASC")
            }else if (!field.isEmpty()){
                if(order == null || order.isEmpty() || order.equals("DESC")){
                    // set pageable sort by field
                    pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,field));
                }else {
                    pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,field));
                }
            }
            //Lấy dữ liệu đã phân trang
            ans = sensorDataRepository.findLimited(pageable);
        }else {
            if(order == null || order.isEmpty() || order.equals("DESC")){
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,field));
            }else {
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,field));
            }
            ans = sensorDataRepository.filterSensorData(pageable,field, term);
        }
        ans = sortDataSensor(ans,field,order);
        return ResponseEntity.ok(ans);
    }
    @GetMapping("/historyaction")
    public ResponseEntity<List<HistoryAction>> historyaction(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "term", required = false) String term
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"timeConvert"));
        List<HistoryAction> ans = new ArrayList<>();

        if(field == null){
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"timeConvert"));
        }
        else if(field.equals("all")){
            ans = actionRepository.filterAllFieldHistoryAction(pageable,term);
            return ResponseEntity.ok(ans);
        }
        if(term == null || term.isEmpty() ){
            if(field == null || field.isEmpty()){
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "timeConvert"));
            }else if (!field.isEmpty()){
                if(order == null || order.isEmpty() || order.equals(Constant.DESC)){
                    pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,field));
                }else {
                    pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,field));
                }
            }
            ans = actionRepository.findLimited(pageable);
        }else {
            if(order == null || order.isEmpty() || order.equals(Constant.DESC)){
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,field));
            }else {
                pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,field));
            }
            ans = actionRepository.filterHistoryAction(pageable,field, term);
        }
        ans = sortHistoryAction(ans,field,order);
        return ResponseEntity.ok(ans);
    }
    //Điều khiển các thiết bị
    @GetMapping("/led")
    public ResponseEntity<HistoryAction> led(@RequestParam(value = "id") int id,
                                             @RequestParam(value = "action") String action) throws InterruptedException {
        //id thiết bị, hành động bật/tắt
        String topic = "";
        //Dựa trên ID của thiết bị, API sẽ xác định topic cần gửi thông điệp
        switch (id) {
            case 1:
                topic = Constant.LED_CONTROL;
                break;
            case 2:
                topic = Constant.FAN_CONTROL;
                break;
            case 3:
                topic = Constant.AC_CONTROL;
                break;
//            case 4:
//                topic = Constant.WARNING_CONTROL;
//                break;+
            default:
                break;
        }
//        Dựa trên giá trị của tham số action ("true" hoặc "false"), API sẽ tạo thông điệp tương ứng với giá trị "1" (bật) hoặc "0" (tắt).
//                Thông điệp này được gửi đến topic tương ứng bằng cách sử dụng phương thức publishMessage từ mosquittoService.
        String mes = action.equals("true") ? "1" : "0";
        // pub mes
        mosquittoService.publishMessage(topic, mes);
        //Chờ thiết bị thực hiện hành động rồi kiểm tra tạng thái
        Thread.sleep(2000);
        while (true) {
            // get last id from sharedList at MosquittoService
            int lastId = Constant.sharedList.get(Constant.sharedList
                    .size() - 1);
            // get history action from DB by lastId
            HistoryAction historyAction = historyActionRepository.findById(lastId);
            Device device = deviceRepository.findById(id); //Tìm kiếm thiết bị từ cơ sở dữ liệu theo ID
            device.setStatus(historyAction.getAction());//Cập nhật trạng thái thiết bị

            // check condition
            //Nếu ID thiết bị trong lịch sử hành động (historyAction.getDevice().getId())
            // trùng với ID thiết bị yêu cầu (id), tức là thiết bị đã thực hiện đúng hành động (bật/tắt)
            // và kết quả đã được lưu vào cơ sở dữ liệu.
            if (historyAction.getDevice().getId().equals(id)) {
                deviceRepository.save(device);
                return ResponseEntity.ok(historyAction);
            } else {
                Thread.sleep(500);
            }
        }
    }


    @GetMapping("/countgreater80")
    public ResponseEntity<Long> countgreater80() throws InterruptedException {
        // get time local
        String time = Time.getTimeLocalConvert();
        // split time to
        time = time.split(" ")[0];
        Long ans = sensorDataRepository.countWindyGreaterThan80(time);
        return ResponseEntity.ok(ans);

    }
    @GetMapping("/counttimeon")
    public ResponseEntity<Long> counttimeon() throws InterruptedException {
        String time = Time.getTimeLocalConvert();
        time = time.split(" ")[0];
        long ans = historyActionRepository.countTrueStatusForFanToday(time);
        return ResponseEntity.ok(ans);
    }

    private List<SensorData> sortDataSensor(List<SensorData> list, String field,String order) {
        //Sắp xếp danh sách SensorData theo trường và dữ liệu tìm kiếm được chỉ định
        if(field == null){
            //Không có trường chỉ định thì trả luôn list ban đầu
            return list;
        }
        else if (field.equals("temperature")) {

            if(order == null || order.equals(Constant.DESC)){
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTemperature().compareTo(o2.getTemperature()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTemperature().compareTo(o1.getTemperature());
                    }
                });
            }else {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTemperature().compareTo(o2.getTemperature()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o1.getTemperature().compareTo(o2.getTemperature());
                    }
                });
            }

        } else if (field.equals("humidity")) {
            if(order == null || order.equals(Constant.DESC)){
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getHumidity().compareTo(o2.getHumidity()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getHumidity().compareTo(o1.getHumidity());
                    }
                });
            }else {

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getHumidity().compareTo(o2.getHumidity()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getHumidity().compareTo(o2.getHumidity());
                    }
                });
            }

        } else if (field.equals("light")) {
            if(order == null || order.equals(Constant.DESC)){

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getLight().compareTo(o2.getLight()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getLight().compareTo(o1.getLight());
                    }
                });
            }else {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getLight().compareTo(o2.getLight()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getLight().compareTo(o2.getLight());
                    }
                });
            }

        } else if (field.equals("time")) {
            if(order == null || order.equals(Constant.DESC)){
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTimeConvert().compareTo(o2.getTimeConvert()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTimeConvert().compareTo(o1.getTimeConvert());
                    }
                });
            }else {

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTimeConvert().compareTo(o2.getTimeConvert()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getTimeConvert().compareTo(o2.getTimeConvert());
                    }
                });
            }

        }
        return list;
    }


    private List<HistoryAction> sortHistoryAction(List<HistoryAction> list, String field,String order){
        if(field == null){
            return list;
        }
        else if(field.equals("name")){
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getName().equals(o2.getName())) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getName().compareTo(o1.getName());
                    }
                });
            } else {

                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getName().equals(o2.getName())) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        }
        else if(field.equals("time")){
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getTimeConvert().equals(o2.getTimeConvert())) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTimeConvert().compareTo(o1.getTimeConvert());
                    }
                });
            } else {

                list.sort(new Comparator<HistoryAction>() {

                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getTimeConvert().equals(o2.getTimeConvert())) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getTimeConvert().compareTo(o2.getTimeConvert());
                    }
                });
            }
        }
        return list;
    }
}
