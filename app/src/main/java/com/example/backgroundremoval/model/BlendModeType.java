package com.example.backgroundremoval.model;

public enum BlendModeType {

    HUE,
    COLOR,
    SOFT_LIGHT;

    public BlendMode createWithAlpha(int alpha) {
        return new BlendMode(this, alpha);
    }

    public BlendMode create() {
        return new BlendMode(this, 255);
    }
}
