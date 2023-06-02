package com.thesis.deliverytracking.misc;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class CustomUnderlineSpan extends CharacterStyle {

    private int underlineColor;

    public CustomUnderlineSpan(int underlineColor) {
        this.underlineColor = underlineColor;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setUnderlineText(true);
        tp.setColor(underlineColor);
        tp.linkColor = underlineColor;
    }
}
