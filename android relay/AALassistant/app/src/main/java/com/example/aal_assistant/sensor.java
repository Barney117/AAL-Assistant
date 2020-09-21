package com.example.aal_assistant;

public class sensor {

    double X_data, Y_data, Z_data;
    private String Time;

    public sensor() {
    }

    /**
     * Constructor for a basic measurement.
     *
     * @param X_data the x axis acceleration
     * @param Y_data the y axis acceleration
     * @param Z_data the z axis acceleration
     * */
    public sensor(double X_data, double Y_data, double Z_data) {
        this.X_data = X_data;
        this.Y_data = Y_data;
        this.Z_data = Z_data;

    }

    public double getCombined() {
        return Math.sqrt(X_data * X_data + Y_data * Y_data + Z_data * Z_data);
    }

   }
