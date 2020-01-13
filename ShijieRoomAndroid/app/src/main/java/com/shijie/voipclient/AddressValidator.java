package com.shijie.voipclient;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.regex.Pattern;

public class AddressValidator implements TextWatcher {

    /**
     * validation pattern.
     */
    public static final Pattern PATTERN = Pattern.compile(
            "[a-zA-Z0-9.]{0,60}"
    );

//    public static final Pattern PATTERN = Pattern.compile(
//            "[A-Za-z0-9_\\-\\u4e00-\\u9fa5]+"
//    );
    private boolean mIsValid = false;

    public boolean isValid() {
        return mIsValid;
    }

    /**
     * Validates if the given input is a valid roomId or uid.
     *
     * @param id       roomId or uid
     * @return {@code true} if the input is a valid roomId or uid. {@code false} otherwise.
     */
    public static boolean isValid(CharSequence id) {
        return id != null && PATTERN.matcher(id).matches();
    }

    @Override
    final public void afterTextChanged(Editable editableText) {
        mIsValid = isValid(editableText);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) {/*No-op*/}

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) {/*No-op*/}
}
