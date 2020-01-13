package com.shijie.voipclient;

import java.util.*;

import android.annotation.SuppressLint;
import android.util.DisplayMetrics;
import android.widget.GridView;
import android.widget.ImageView;

import android.media.AudioManager;
import android.app.Activity;
import android.content.Context;

import android.graphics.Color;
import android.graphics.Point;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.shijie.devicemanager.DeviceCallback;
import com.shijie.room.Room.EngineErrorTypeT;
import com.shijie.room.Room;
import com.shijie.room.RoomCallback;
import com.shijie.devicemanager.DeviceManager;
import com.shijie.rendermanager.RenderManager;
import com.shijie.rendermanager.videoRender.VideoView;
import com.shijie.room.Room.ClientRole;
import com.shijie.utils.SystemUtil;
import android.widget.Toast;


@SuppressLint("NewApi")
public class VideoCallActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "VideoCallActivity";

    private Room room = null;
    private DeviceManager deviceManager;
    private RenderManager renderManager;
    private RoomCallback roomCallback;
    private DeviceCallback deviceCallback;

    private boolean hasInited = false;
    private boolean localViewShowed = false;

    private RelativeLayout localPreview;

    List<RemoteVideoView> remoteVideoViewList = new ArrayList<>();

    //bottom container
    RemoteVideoViewAdapter adapter;
    private GridView remoteVideoGridView;
    private boolean videoMuted = false;
    private boolean audioMuted = false;
    private boolean linked = false;
    ImageView btn_switch_camera;
    ImageView btn_microphone ;
    ImageView btn_terminate;
    ImageView btn_video;
    ImageView btn_switch_role;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hasInited = false;
        populateUi();
        //why user need invoke this ?
        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        constructSession();
        joinRoom();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "the application onDestroy ");
        room.leaveRoom();
        for (int i = 0; i < remoteVideoViewList.size(); i++) {
            unbindViewWithUid(remoteVideoViewList.get(i).uid);
        }
        remoteVideoViewList.clear();
        stopPreview("");
        destructSession();
        super.onDestroy();
    }

    public void constructSession() {
        SystemUtil.Init(getApplicationContext(), null, Constant.APP_ID);
        roomCallback = new RoomListener();
        deviceCallback = new DeviceListener();
        deviceManager = new DeviceManager(getApplicationContext());
        deviceManager.attachCallback(deviceCallback);
        deviceManager.enableHeadsetPlugAutoHandler(true);
        renderManager = new RenderManager(getApplicationContext());
        room = new Room(getApplicationContext(), deviceManager.getNativeInstance());
        room.attachCallback(roomCallback);
        hasInited = true;
        Log.d(TAG, "constructSession success!");
    }


    public void destructSession() {
        if (hasInited == true) {
            room.detachCallback();
            room.destroy();
            deviceManager.detachCallback();
            deviceManager.stopAudioDevice();
            deviceManager.stopCamera();
            deviceManager.destroy();
            renderManager.destroy();
            hasInited = false;
            SystemUtil.UnInit();
        }
    }

    public void startPreview(String id) {
        Log.d(TAG, "start local Preview");
        if (localViewShowed == true) {
            return;
        }
        localViewShowed = true;
        bindViewWithUid(""); 
    }

    public void stopPreview(String id) {
        Log.d(TAG, "stop local Preview :");
        if (localViewShowed == false) {
            return;
        }
        localViewShowed = false;
        unbindViewWithUid("");
    }

    private void joinRoom() {
        QueueHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                Room.Profile profile = RoomModel.getInstance().getProfile();
                room.joinRoom(
                        RoomModel.getInstance().getRoomId(),
                        RoomModel.getInstance().getUid(),
                        profile,
                        Constant.DEFAULT_APPKEY);

                // Viewer doesn't need to start device and camera
                if (profile.clientRole == ClientRole.CLIENT_ROLE_VIEWER) {
                    Log.d(TAG, "User join room as viewer, so just playout audio device.");
                    deviceManager.startAudioPlayoutDevice();
                    return;
                }

                deviceManager.setTargetVideo(profile.videoWidth, profile.videoHeight, Constant.FRAME_RATE);
                room.setResolution(profile.videoWidth, profile.videoHeight, Constant.FRAME_RATE, true);
                deviceManager.startAudioDevice();
                room.unMuteMicrophone();
                deviceManager.setSpeaker(true);
                deviceManager.unMuteSpeaker();
                if (!profile.joinWithoutVideo) {
                    deviceManager.startCamera();
                }
            }
        });
    }




    @SuppressLint("ClickableViewAccessibility")
    private void populateUi() {
        setContentView(R.layout.activity_video_call);

        localPreview = (RelativeLayout) findViewById(R.id.localView);
        remoteVideoGridView = (GridView) findViewById(R.id.remote_view_group);
        adapter = new RemoteVideoViewAdapter(getApplicationContext(), remoteVideoViewList);
        remoteVideoGridView.setAdapter(adapter);
        remoteVideoGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });

        populateBottomContainer();
    }

    private void populateBottomContainer() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int bottomBts = 5;
        int bottom_btn_margin = (int)getResources().getDimension(R.dimen.bottom_btn_margin);
        int bottom_btn_width = (int)getResources().getDimension(R.dimen.bottom_btn_width);
        int bottom_btn_high = (int)getResources().getDimension(R.dimen.bottom_btn_high);

        int margin = (width - 2 * bottom_btn_margin - bottomBts * bottom_btn_width) / (bottomBts - 1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(bottom_btn_width , bottom_btn_high);
        lp.setMargins(margin, 0, 0, 0);

        btn_switch_camera = (ImageView) findViewById(R.id.btn_switch_camera);
        btn_switch_camera.setOnClickListener(this);

        btn_microphone = (ImageView) findViewById(R.id.btn_micphone);
        btn_microphone.setLayoutParams(lp);
        btn_microphone.setOnClickListener(this);

        btn_terminate = (ImageView) findViewById(R.id.btn_terminate);
        btn_terminate.setLayoutParams(lp);
        btn_terminate.setOnClickListener(this);

        btn_video = (ImageView) findViewById(R.id.btn_video);
        btn_video.setLayoutParams(lp);
        btn_video.setOnClickListener(this);

        btn_switch_role = (ImageView) findViewById(R.id.btn_switch_role);
        btn_switch_role.setLayoutParams(lp);
        btn_switch_role.setOnClickListener(this);

        if (RoomModel.getInstance().isOnlyAudio() || RoomModel.getInstance().getClientRole() == ClientRole.CLIENT_ROLE_VIEWER) {
            btn_video.setEnabled(false);
            btn_switch_camera.setEnabled(false);
        }
    }

    private void bindViewWithUid(String uid) {
        final String id = uid;
        Log.i(TAG, "bindViewWithUid: " + id);
        int column = getColumnBySize(remoteVideoViewList.size() + 1);
        int width = localPreview.getWidth() / column;
        int height = localPreview.getHeight() / column;
        updateViewSize(width, height);
        Point size = new Point(width, height);
        VideoView render = renderManager.createRender(size);
        RemoteVideoView remoteVideoView = new RemoteVideoView(width, height, id, render);
        remoteVideoViewList.add(remoteVideoView);
        remoteVideoGridView.setNumColumns(column);
        adapter.notifyDataSetChanged();
        remoteVideoGridView.invalidateViews();
        renderManager.bindRenderWithStream(render, id);
    }

    private void unbindViewWithUid(String id) {
        Log.i(TAG,"unbindViewWithUid id: " + id);
        int column = getColumnBySize(remoteVideoViewList.size() - 1);
        updateViewSize(localPreview.getWidth() / column, localPreview.getHeight() / column);
        remoteVideoGridView.setNumColumns(column);
        Iterator<RemoteVideoView> it = remoteVideoViewList.iterator();
        while (it.hasNext()) {
            if (it.next().uid.equals(id)) {
                it.remove();
            }
        }
        adapter.notifyDataSetChanged();
        remoteVideoGridView.invalidateViews();
        VideoView render = renderManager.getRender(id);
        if (render != null) {
            render.setVisibility(View.INVISIBLE);
            renderManager.unbindRenderWithStream(render);
            renderManager.destroyRender(render);
        }

    }

    private int getColumnBySize(int num) {
        int column = 1;
        if (num >= 5) {
            column = 3;
        } else if (num >= 2) {
            column = 2;
        } else if (num == 1) {
            column = 1;
        }
        return column;
    }

    private void updateViewSize(int newWidth, int newHeight) {

        for (RemoteVideoView rv : remoteVideoViewList ) {
            rv.height = newHeight;
            rv.width = newWidth;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_terminate:
                handleTerminateBtClicked();
                break;
            case R.id.btn_switch_camera:
                handleSwitchCameraClicked();
                break;
            case R.id.btn_micphone:
                handleMicrophoneClicked();
                break;
            case R.id.btn_video:
                handleVideoClicked();
                break;
            case R.id.btn_switch_role:
                handleLinkClicked();
                break;
            default:
                break;

        }
    }


    private void handleTerminateBtClicked() {
        room.leaveRoom();
        for (int i = 0; i < remoteVideoViewList.size(); i++) {
            unbindViewWithUid(remoteVideoViewList.get(i).uid);
        }
        remoteVideoViewList.clear();
        stopPreview("");
        finish();
    }

    private void handleSwitchCameraClicked() {
        deviceManager.switchCamera();
    }

    private void handleMicrophoneClicked() {
        if (audioMuted == false) {
            audioMuted = true;
            deviceManager.muteMicrophone();
            btn_microphone.setImageDrawable(getDrawable(R.drawable.mute_micphone));
        } else {
            audioMuted = false;
            deviceManager.unMuteMicrophone();
            btn_microphone.setImageDrawable(getDrawable(R.drawable.micphone));
        }
    }

    private void handleVideoClicked() {
        if (videoMuted == false) {
            videoMuted = true;
            deviceManager.stopCamera();
            btn_video.setImageDrawable(getDrawable(R.drawable.video_off));
        } else {
            videoMuted = false;
            deviceManager.startCamera();
            btn_video.setImageDrawable(getDrawable(R.drawable.video));
        }
    }

    private void handleLinkClicked() {
        if (RoomModel.getInstance().getClientRole() == ClientRole.CLIENT_ROLE_ATTENDEE ) {
            showToastMessage(R.string.link_wrong_role_tip, Toast.LENGTH_LONG);
            btn_switch_role.setEnabled(false);
            return;
        }
        if (RoomModel.getInstance().getClientRole() == ClientRole.CLIENT_ROLE_COHOST) {
            room.switchClientRole(ClientRole.CLIENT_ROLE_VIEWER);
            RoomModel.getInstance().setClientRole(ClientRole.CLIENT_ROLE_VIEWER);
            btn_video.setEnabled(false);
            btn_switch_camera.setEnabled(false);
            //btn_switch_role.setImageDrawable(getDrawable(R.drawable.unlinked));
        } else {
            //btn_switch_role.setImageDrawable(getDrawable(R.drawable.link));
            room.switchClientRole(ClientRole.CLIENT_ROLE_COHOST);
            RoomModel.getInstance().setClientRole(ClientRole.CLIENT_ROLE_COHOST);
            if (!RoomModel.getInstance().isOnlyAudio()) {
                deviceManager.startCamera();
                btn_video.setEnabled(true);
                btn_switch_camera.setEnabled(true);
            }

        }
    }

    private void showToastMessage(int resId, int duration) {
        Toast.makeText(getApplicationContext(), resId, duration).show();
    }

    private void showToastMessage(CharSequence s, int duration) {
        Toast.makeText(getApplicationContext(), s, duration).show();
    }

    private class RoomListener implements RoomCallback {
        @Override
        public void onUsersVolumeChanged(UserVolume[] userVolumes) {

        }

        @Override
        public void onSpeakingStatusChanged(String s, boolean i) {

        }

        @Override
        public void onRemoteUserJoined(String s) {

        }

        @Override
        public void onRemoteUserLeaved(String s) {

        }

        @Override
        public void onError(final String error_desc, EngineErrorTypeT engineErrorTypeT) {
            final String s = error_desc;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (s.contains("conflict")) {
                        showToastMessage(R.string.kicked_tip, Toast.LENGTH_LONG);
                    } else if (!error_desc.isEmpty()){
                        showToastMessage(s, Toast.LENGTH_LONG);
                    }
                }
            });
        }

        @Override
        public void onRemoteVideoResize(String s, int i, int i1) {

        }

        @Override
        public void onLocalUserLeaved(EngineErrorTypeT var1) {

        }

        @Override
        public void onLocalUserJoined() {

        }

        @Override
        public void onFirstVideoFrameReceived(String uid) {
            Log.e(TAG, "onGetFirstVideoSample user name is " + uid);

            final String id = uid;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindViewWithUid(id);
                }
            });
        }

        @Override
        public void onRemoteVideoStreamRemoved(String uid, String streamId) {
            final String id = uid;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    unbindViewWithUid(id);
                }
            });
        }
    }

    private class DeviceListener implements DeviceCallback {
        @Override
        public void onCameraStarted() {
            Log.i(TAG, "devicemanager callback onStartCamera");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    startPreview("");
                }
            });
        }

        @Override
        public void onCameraStopped() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    stopPreview("");
                }
            });

        }

        @Override
        public void onMicStartFailed() {

        }

        @Override
        public void onCameraStartFailed() {

        }

        @Override
        public void onAudioMixedMusicFinished() {

        }
    }

}


