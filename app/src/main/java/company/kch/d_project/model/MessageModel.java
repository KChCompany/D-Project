package company.kch.d_project.model;

import java.util.Date;

public class MessageModel {
    public String userName, massage, key;
    public Date time;
    public double latitude, longitude;

    public MessageModel(){};

    public MessageModel(String userName, String message, Date time, double latitude, double longitude, String key) {
        this.userName = userName;
        this.massage = message;
        this.key = key;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;

    }
}
