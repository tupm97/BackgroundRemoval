package com.example.backgroundremoval.lib;

public class BlendMode {

    private BlendModeType type;
    private int alpha;

    public BlendMode(BlendModeType type, int alpha) {
        this.type = type;
        this.alpha = alpha;
    }

    public BlendModeType getType() {
        return type;
    }

    public int getAlpha() {
        return alpha;
    }
}
