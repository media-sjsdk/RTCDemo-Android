package com.shijie.voipclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;


public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, Spinner.OnItemSelectedListener{
    private EditText roomIdText;
    private EditText userIdText;
    private EditText serverText;
    private InputValidator roomIdValidator;
    private InputValidator userIdValidator;
    private AddressValidator serverValidator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RoomModel.getInstance().loadData(getPreferences(Context.MODE_PRIVATE));
        populateUi();
    }

    @Override
    public void onDestroy() {
        RoomModel.getInstance().saveData(getPreferences(Context.MODE_PRIVATE));
        super.onDestroy();
    }
    private void populateUi() {
        Spinner resolutionSpinner = (Spinner) findViewById(R.id.resolutionSp);
        resolutionSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.resolution_items)));
        resolutionSpinner.setOnItemSelectedListener(this);
        resolutionSpinner.setSelection(RoomModel.getInstance().getResolutionChoice());

        Spinner clientRoleSpinner = (Spinner)findViewById(R.id.clientRoleSp);
        clientRoleSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.role_items)));
        clientRoleSpinner.setOnItemSelectedListener(this);
        clientRoleSpinner.setSelection(RoomModel.getInstance().getClientRoleChoice());


        roomIdValidator = new InputValidator();
        userIdValidator = new InputValidator();
        serverValidator = new AddressValidator();
        roomIdText = (EditText) findViewById(R.id.roomId);
        userIdText = (EditText)findViewById(R.id.userName);
        serverText = (EditText)findViewById(R.id.serverAddressId);
        roomIdText.addTextChangedListener(roomIdValidator);
        userIdText.addTextChangedListener(userIdValidator);
        serverText.addTextChangedListener(serverValidator);
        roomIdText.setText(RoomModel.getInstance().getRoomId());
        userIdText.setText(RoomModel.getInstance().getUid());
        serverText.setText(RoomModel.getInstance().getServer());


        CheckBox videoCB = (CheckBox) findViewById(R.id.withoutVideo);
        videoCB.setChecked(RoomModel.getInstance().isOnlyAudio());
        videoCB.setOnCheckedChangeListener(this);

        findViewById(R.id.join_button).setOnClickListener(this);
    }

    private void handleResolutionSelected(int position) {
        if (position >= 0) {
            RoomModel.getInstance().setResolutionChoice(position);
        }
    }

    private void handleClientRoleSelected(int position) {
        if (position >= 0) {
            RoomModel.getInstance().setClientRoleChoice(position);
        }
    }

    private void handleWithoutVideoStatusChanged(boolean enable) {
        RoomModel.getInstance().setOnlyAudio(enable);
    }

    private void handleJoinButtonClicked() {
        if (checkConditions()) {
            RoomModel.getInstance().saveData(getPreferences(Context.MODE_PRIVATE));
            Intent intent = new Intent(this, VideoCallActivity.class);
            startActivity(intent);
        }
    }

    private boolean checkConditions() {

        if (!roomIdValidator.isValid()) {
            if (roomIdText.getText().length() == 0) {
                roomIdText.setError(getString(R.string.empty_hint));
            } else {
                roomIdText.setError(getString(R.string.room_tip));
            }
            return false;
        } else {
            RoomModel.getInstance().setRoomId(roomIdText.getText().toString());
        }

        if (!userIdValidator.isValid()) {
            if (userIdText.getText().length() == 0) {
                userIdText.setError(getString(R.string.empty_hint));
            } else {
                userIdText.setError(getString(R.string.user_tip));
            }
            return false;
        } else {
            RoomModel.getInstance().setUid(userIdText.getText().toString());
        }

        if (!serverValidator.isValid()) {
            serverText.setError(getString(R.string.server_address_hit));
            return false;
        } else {
            RoomModel.getInstance().setServer(serverText.getText().toString());
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.join_button:
                handleJoinButtonClicked();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.withoutVideo:
                handleWithoutVideoStatusChanged(isChecked);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.resolutionSp:
                handleResolutionSelected(position);
                break;
            case R.id.clientRoleSp:
                handleClientRoleSelected(position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
