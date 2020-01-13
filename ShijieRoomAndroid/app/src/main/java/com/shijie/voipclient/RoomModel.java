package com.shijie.voipclient;

import android.content.SharedPreferences;

import com.shijie.room.Room;
import com.shijie.room.Room.ClientRole;

public class RoomModel {

    private static RoomModel instance = new RoomModel();

    public static RoomModel getInstance(){
        return instance;
    }

    static final String KEY_UID = "user_id";
    static final String KEY_ROOM_ID = "room_id";
    static final String KEY_WITHOUT_VIDEO = "without_video";
    static final String KEY_RESOLUTION_CHOICE = "resolution_choice";
    static final String KEY_CLIENT_ROLE_CHOICE = "client_role_choice";
    static final String KEY_SERVER = "server_address";

    public void loadData(SharedPreferences sharedPreferences) {
        clientRoleChoice = sharedPreferences.getInt(KEY_CLIENT_ROLE_CHOICE, 2);
        resolutionChoice = sharedPreferences.getInt(KEY_RESOLUTION_CHOICE, 4);
        roomId = sharedPreferences.getString(KEY_ROOM_ID, "8787");
        uid = sharedPreferences.getString(KEY_UID, String.valueOf(System.currentTimeMillis() % 1000000));
        onlyAudio = sharedPreferences.getBoolean(KEY_WITHOUT_VIDEO, false);
        server = sharedPreferences.getString(KEY_SERVER, "mcu.sjsdk.com");
    }

    public void saveData(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_CLIENT_ROLE_CHOICE, clientRoleChoice);
        editor.putInt(KEY_RESOLUTION_CHOICE, resolutionChoice);
        editor.putBoolean(KEY_WITHOUT_VIDEO, onlyAudio);
        editor.putString(KEY_ROOM_ID, roomId);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_SERVER, server);
        editor.commit();
    }


    public Room.Profile getProfile() {
        Room.Profile profile = new Room.Profile();
        profile.enableAdaptiveResolution = adaptive;
        profile.clientRole = clientRole;
        profile.joinWithoutVideo = onlyAudio;
        profile.videoHeight = videoHigh;
        profile.videoWidth = videoWidth;
        profile.serverAddress = server;
        return profile;
    }

    public int getVideoHigh() {
        return videoHigh;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public boolean isOnlyAudio() {
        return onlyAudio;
    }

    public Room.ClientRole getClientRole() {
        return clientRole;
    }

    public void setVideoHigh(int videoHigh) {
        this.videoHigh = videoHigh;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void setOnlyAudio(boolean onlyAudio) {
        this.onlyAudio = onlyAudio;
    }

    public void setClientRole(Room.ClientRole clientRole) {
        this.clientRole = clientRole;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        return this.server;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setResolutionChoice(int choice) {
        resolutionChoice = choice;
        videoHigh = (choice / 2 + 1) * 320;
        videoWidth = (videoHigh) * 9 / 16;
        adaptive = choice % 2 == 0 ? false : true;
    }

    public void setClientRoleChoice(int choice) {
        clientRoleChoice = choice;
        switch (choice) {
            case 0:
                clientRole = ClientRole.CLIENT_ROLE_COHOST;
                break;
            case 1:
                clientRole = Room.ClientRole.CLIENT_ROLE_VIEWER;
                break;
            case 2:
                clientRole = Room.ClientRole.CLIENT_ROLE_ATTENDEE;
                break;
            default:
                clientRole = ClientRole.CLIENT_ROLE_ATTENDEE;
                break;
        }
    }

    public int getResolutionChoice() {
        return resolutionChoice;
    }

    public int getClientRoleChoice() {
        return clientRoleChoice;
    }

    public boolean isAdaptive() {
        return adaptive;
    }

    private int videoHigh;
    private int videoWidth;
    private boolean onlyAudio;
    private Room.ClientRole clientRole;
    private String server;
    private boolean adaptive;
    private int resolutionChoice;
    private int clientRoleChoice;
    private String uid;
    private String roomId;
}
