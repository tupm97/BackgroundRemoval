package com.example.backgroundremoval.model;

class NotInitializedException extends RuntimeException {
    private static final String ANDROID_GET_STARTED_DOCS_LINKS = "https://docs.fritz.ai/get-started.html#android";

    public NotInitializedException() {
        super("Fritz is not initialized. Make sure you've initialized your app with fritz_api_key: " + ANDROID_GET_STARTED_DOCS_LINKS);
    }
}
