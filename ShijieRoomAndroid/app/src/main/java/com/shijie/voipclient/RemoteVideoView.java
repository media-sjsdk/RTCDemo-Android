package com.shijie.voipclient;


import com.shijie.rendermanager.videoRender.VideoView;

public class RemoteVideoView {

    public int width;
    public int height;
    public String uid;
    public VideoView videoView;

    public RemoteVideoView(int width, int height, String uid) {
        this.uid = uid;
        this.width = width;
        this.height = height;
    }

    public RemoteVideoView(int width, int height, String uid, VideoView videoView) {
        this.uid = uid;
        this.width = width;
        this.height = height;
        this.videoView = videoView;
    }

}
