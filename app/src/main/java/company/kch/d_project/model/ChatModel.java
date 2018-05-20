package company.kch.d_project.model;

import java.util.Date;

public class ChatModel {
    public String creator, chatName;
    public Date dateTime;
    public double latitude, longitude;


    public ChatModel(){};

    public ChatModel(String creator, String chatName, Date dateTime, double latitude, double longitude) {
        this.creator = creator;
        this.chatName = chatName;
        this.dateTime = dateTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
