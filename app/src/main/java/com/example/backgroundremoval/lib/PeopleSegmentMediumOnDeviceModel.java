package com.example.backgroundremoval.lib;

import android.os.Environment;

public class PeopleSegmentMediumOnDeviceModel extends SegmentOnDeviceModel {
    private static final String MODEL_ID = FeatureModelID.PEOPLE_SEGMENT_MEDIUM_MODEL_ID;
   // private static final String MODEL_PATH = "file:///android_asset/people_hq_768x768_1_1543441179.tflite";
    private static final String MODEL_PATH = Environment.getExternalStorageDirectory().getPath()+"/viettelbackgroundremoval.tflite";
    private static final int MODEL_VERSION = 1;
    private static final MaskType[] CLASSIFICATIONS = {
            MaskType.NONE,
            MaskType.PERSON,
    };

    public PeopleSegmentMediumOnDeviceModel() {
        super(MODEL_PATH,
                MODEL_ID,
                MODEL_VERSION,
                CLASSIFICATIONS);
    }
}
