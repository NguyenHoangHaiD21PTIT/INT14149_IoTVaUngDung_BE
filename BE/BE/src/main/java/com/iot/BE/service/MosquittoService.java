package com.iot.BE.service;

import com.iot.BE.entity.Device;
import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import com.iot.BE.repository.DeviceRepository;
import com.iot.BE.repository.HistoryActionRepository;
import com.iot.BE.repository.SensorDataRepository;
import com.iot.BE.utils.Constant;
import com.iot.BE.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Arrays;

@Service
public class MosquittoService {
    private MqttClient client;

    // dependency injection
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private HistoryActionRepository historyActionRepository;
    @Autowired
    private SensorDataRepository sensorDataRepository;

    public MosquittoService()  {
        try {
            //Tạo MQTT Client để kêt nối với Broker
            client = new MqttClient(Constant.BROKER, Constant.CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            //Khi kết nối lại, Client sẽ bắt đầu Session mới
            options.setCleanSession(true);

            // Connect to the MQTT broker
            System.out.println("Connecting to broker: " + Constant.BROKER);
            client.connect(options);
            System.out.println("Connected");

            //Xử lý các sự kiện của MQTT
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost! " + cause.getMessage());
                    // Retry connection
                    while (!client.isConnected()) {
                        try {
                            System.out.println("Attempting to reconnect...");
                            client.connect(options);
                            // Resubscribe to topics
                            client.subscribe(Constant.DATA_SENSOR);
                            client.subscribe(Constant.LED_RESPONSE);
                            client.subscribe(Constant.FAN_RESPONSE);
                            client.subscribe(Constant.AC_RESPONSE);
                            //Sua
                            client.subscribe(Constant.LED2_RESPONSE);
                            client.subscribe(Constant.LED3_RESPONSE);
                            System.out.println("Reconnected to MQTT broker");
                        } catch (MqttException e) {
                            System.out.println("Reconnection failed, will retry in 2 seconds.");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                //Có message đến từ 1 topic mà client đã đăng ký thì đc xu ly
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // handle
                    handleMessage(topic,message);
                    System.out.println("Topic: " + topic + ", Message: " + new String(message.getPayload()) );
                }
                //Hoàn tất quá trình gửi
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery complete");
                }
            });

            // Subscribe to topics
            client.subscribe(Constant.DATA_SENSOR);
            client.subscribe(Constant.LED_RESPONSE);
            client.subscribe(Constant.FAN_RESPONSE);
            client.subscribe(Constant.AC_RESPONSE);
            //Sua
            client.subscribe(Constant.LED2_RESPONSE);
            client.subscribe(Constant.LED3_RESPONSE);
        } catch (MqttException  e) {
            e.printStackTrace();
        }
    }

    //Xử lý các thông điệp đã nhận được từ các topic
    private void handleMessage(String topic, MqttMessage message) {
        if (Constant.DATA_SENSOR.equals(topic)) {
            //Lấy dữ liệu từ message
            String data = new String(message.getPayload());
            SensorData sensorData = new SensorData();
            // split data to double array
            // 0 - Temperature
            // 1 - Humidity
            // 2 - Light
            // 3 - News
            Double [] arrayData = Arrays.stream(data.split(" "))
                    .map(Double::valueOf)
                    .toArray(Double[]::new);
            // Set value to fields
            sensorData.setTemperature(arrayData[0]);
            sensorData.setHumidity(arrayData[1]);
            sensorData.setLight(1024 - arrayData[2]);
            sensorData.setTime((Time.getTimeLocal()));
            sensorData.setNews(arrayData[3]);//sua
//            sensorData.setOther(arrayData[3]);
            sensorData.setTimeConvert((Time.getTimeLocalConvert()));
            sensorDataRepository.save(sensorData);

        } else if (Constant.LED_RESPONSE.equals(topic)) {
            // Kiểm tra nếu topic nhận được là LED_RESPONSE
            // Mục đích: Tạo một bản ghi hành động lịch sử (history action) cho thiết bị LED và lưu vào cơ sở dữ liệu
            HistoryAction historyAction = getResponseMQTT(1, message);
            // Save history action
            HistoryAction data = historyActionRepository.save(historyAction);
            // Add id of history action to list
            // Aim: compare the last id with the last id of history action
            Constant.sharedList.add(data.getId());

        } else if (Constant.FAN_RESPONSE.equals(topic)) {
            HistoryAction dataDevice = getResponseMQTT(2, message);
            HistoryAction data = historyActionRepository.save(dataDevice);
            Constant.sharedList.add(data.getId());

        } else if (Constant.AC_RESPONSE.equals(topic)) {
            HistoryAction dataDevice = getResponseMQTT(3, message);
            HistoryAction data = historyActionRepository.save(dataDevice);
            Constant.sharedList.add(data.getId());
        } else if (Constant.LED2_RESPONSE.equals(topic)) {
        HistoryAction dataDevice = getResponseMQTT(4, message);
        HistoryAction data = historyActionRepository.save(dataDevice);
        Constant.sharedList.add(data.getId());
        } else if (Constant.LED3_RESPONSE.equals(topic)) {
        HistoryAction dataDevice = getResponseMQTT(5, message);
        HistoryAction data = historyActionRepository.save(dataDevice);
        Constant.sharedList.add(data.getId());
        }
        //sua

    }

    //Gửi thông điệp đến 1 topic
    public void publishMessage(String topic, String messageContent) {
        try {
            // create message mqtt
            MqttMessage message = new MqttMessage(messageContent.getBytes());
            // quality of service = 2
            message.setQos(2);
            // send mes to topic
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Phương thức này nhận vào id của thiết bị và thông điệp MQTT,
    //sau đó tạo một đối tượng HistoryAction từ thông tin của thiết bị và thông điệp
    private HistoryAction getResponseMQTT(int id, MqttMessage message) {//id: mã thiết bị
        // get data String from mqtt
        String data = new String(message.getPayload());
        //Khởi tạo đối tợng
        HistoryAction historyAction = new HistoryAction();
        //Truy vấn theo ID
        Device device = deviceRepository.findById(id);
        //Tên thiết bị
        historyAction.setDevice(device);
        historyAction.setTime(Time.getTimeLocal());
        historyAction.setName(device.getName());
        // if data == "HIGH" ? true : false
        historyAction.setAction(data.equals("HIGH"));
        return historyAction;
    }
}
