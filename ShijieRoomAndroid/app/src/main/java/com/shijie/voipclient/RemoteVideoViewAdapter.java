package com.shijie.voipclient;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class RemoteVideoViewAdapter extends BaseAdapter {
    private List<RemoteVideoView> remoteVideoViews;
    private Context context;

    RemoteVideoViewAdapter(Context context, List<RemoteVideoView> videoViews) {
        this.context = context;
        this.remoteVideoViews = videoViews;
    }

    @Override
    public int getCount() {
        return remoteVideoViews.size();
    }

    @Override
    public Object getItem(int i) {
        return remoteVideoViews.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.surfaceview_layout, viewGroup, false);
            holder = new ViewHolder();
            holder.name_text_view = (TextView) convertView.findViewById(R.id.remoteName);
            holder.video_view_layout = (RelativeLayout) convertView.findViewById(R.id.video_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ViewGroup.LayoutParams lp = holder.video_view_layout.getLayoutParams();
        lp.height = remoteVideoViews.get(i).height;
        lp.width = remoteVideoViews.get(i).width;
        holder.video_view_layout.setLayoutParams(lp);

        detachView(remoteVideoViews.get(i).videoView);
        holder.video_view_layout.addView(remoteVideoViews.get(i).videoView);
        remoteVideoViews.get(i).videoView.setVisibility(View.VISIBLE);
        remoteVideoViews.get(i).videoView.setZOrderMediaOverlay(true);

        TextView textView = holder.name_text_view;
        textView.setText(remoteVideoViews.get(i).uid);
        textView.bringToFront();
        return convertView;
    }

    private void detachView(com.shijie.rendermanager.videoRender.VideoView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent != null) {
            parent.removeView(videoView);
        }
    }

    private class ViewHolder {
        RelativeLayout video_view_layout;
        TextView name_text_view;
    }
}
