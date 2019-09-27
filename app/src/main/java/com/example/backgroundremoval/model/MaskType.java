package com.example.backgroundremoval.model;

import android.graphics.Color;

public class MaskType {
    public static MaskType NONE = new MaskType("None", Color.TRANSPARENT);
    public static MaskType BUILDING_EDFICE = new MaskType("Building. Edifice", Color.GRAY);
    public static MaskType SKY = new MaskType("Sky", Color.RED);
    public static MaskType TREE = new MaskType("Tree", Color.parseColor("#4ca64c")); // mid green
    public static MaskType SIDEWALK_PAVEMENT = new MaskType("Sidewalk, Pavement", Color.DKGRAY);
    public static MaskType EARTH_GROUND = new MaskType("Earth, Ground", Color.parseColor("#004000")); // dark green
    public static MaskType CAR = new MaskType("Car, Auto, Automobile, Machine, Motorcar", Color.parseColor("#FFDB99")); //  light orange
    public static MaskType WATER = new MaskType("Water", Color.BLUE);
    public static MaskType HOUSE = new MaskType("House", Color.parseColor("#A64CA6")); // purple
    public static MaskType FENCE = new MaskType("Fence, Fencing", Color.WHITE);
    public static MaskType SIGNBOARD = new MaskType("Signboard, Sign", Color.parseColor("#FFC0CB")); // pink
    public static MaskType SKYSCRAPER = new MaskType("Skyscraper", Color.LTGRAY);
    public static MaskType BRIDGE = new MaskType("Bridge, Span", Color.parseColor("#F4A460")); // orange
    public static MaskType RIVER = new MaskType("River", Color.parseColor("#6666ff")); // mid blue
    public static MaskType BUS = new MaskType("bus, autobus, coach, charabanc, double-decker, jitney, motorbus, motorcoach, omnibus, passenger vehicle", Color.parseColor("#ffa500")); // dark orange
    public static MaskType TRUCK = new MaskType("truck, motortruck", Color.parseColor("#332100")); // dark brown
    public static MaskType VAN = new MaskType("van", Color.parseColor("#FFC04C")); // normal orange
    public static MaskType MINIBIKE = new MaskType("minibike, motorbike", Color.BLACK);
    public static MaskType BICYCLE = new MaskType("bicycle, bike, wheel, cycle", Color.parseColor("#334045")); // dark blue
    public static MaskType TRAFFIC_LIGHT = new MaskType("traffic light, traffic signal, stoplight", Color.YELLOW);
    public static MaskType PERSON = new MaskType("person, individual, someone, somebody, mortal, soul", Color.CYAN);

    public static MaskType CHAIR = new MaskType("Chair", Color.parseColor("#F4A460")); // Sandy Brown
    public static MaskType WALL = new MaskType("Wall", Color.WHITE);
    public static MaskType COFFEE_TABLE = new MaskType("Coffee Table", Color.parseColor("#A52A2A")); // Brown
    public static MaskType CEILING = new MaskType("Ceiling", Color.LTGRAY);
    public static MaskType FLOOR = new MaskType("Floor", Color.DKGRAY);
    public static MaskType BED = new MaskType("Bed", Color.parseColor("#add8e6")); // Light Blue
    public static MaskType LAMP = new MaskType("Lamp", Color.YELLOW);
    public static MaskType SOFA = new MaskType("Sofa", Color.RED);
    public static MaskType WINDOW = new MaskType("Window", Color.CYAN);
    public static MaskType PILLOW = new MaskType("Pillow", Color.parseColor("#FFE4C4")); // beige

    public static MaskType HAIR = new MaskType("Hair", Color.RED);
    public static MaskType PET = new MaskType("Pet", Color.BLUE);

    public String label;
    public int color;

    /**
     * Constructor for MaskType.
     *
     * @param label
     * @param color
     * @hide
     */
    public MaskType(String label, int color) {
        this.label = label;
        this.color = color;
    }

    /**
     * The color to use for the mask.
     *
     * @return the color value
     * @hide
     */
    public int getColorIdentifier() {
        return color;
    }

    /**
     * The label for the mask.
     *
     * @return a string
     * @hide
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the color you'd like for this mask type.
     *
     * @param color
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Checks if the custom model are equal.
     *
     * @param other The other MaskType
     * @return true if equal, false otherwise
     * @hide
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        MaskType maskType = (MaskType) other;
        return this.hashCode() == maskType.hashCode();
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
