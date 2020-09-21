package com.example.aal_assistant;

public class representations {

    public static double[] convertAccelerometerdata(byte[] value) {
        final float G_UNIT = 32768 / 8;

        int X = (value[7] << 8) + value[6];
        int y = (value[9] << 8) + value[8];
        int z = (value[11] << 8) + value[10];
        return new double[]{((X / G_UNIT) * -1), y / G_UNIT, ((z / G_UNIT) * -1)};
    }
}

