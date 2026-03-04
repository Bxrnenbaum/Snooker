package com.example.snooker;

public class Vector2 {
    double x;
    double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 sum(Vector2 a, Vector2 b) { //returns the sum of vector a and vector b
        return new Vector2(a.x + b.x, a.y + b.y);
    }

    public static Vector2 difference(Vector2 a, Vector2 b) { //returns the difference between vector a and vector b
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    public static Vector2 abs(Vector2 a) { //returns the absolute of the vector
        return new Vector2(Math.abs(a.x), Math.abs(a.y));
    }

    public static Vector2 normalized(Vector2 a){ //returns the normalized vector
        double magnitude = Math.sqrt(a.x * a.x + a.y * a.y);
        if(magnitude == 0)
            return new Vector2(0, 0);

        return new Vector2(a.x / magnitude, a.y / magnitude);
    }

    public static double dot(Vector2 a, Vector2 b){ //returns the dot product of the vectors
        return a.x * b.x + a.y * b.y;
    }

    public static Vector2 scalar(double scalar, Vector2 a){ // returns a vector where each component has been multiplied by the scalar
        return new Vector2(scalar * a.x, scalar * a.y);
    }

    public static double distance(Vector2 a, Vector2 b){ //returns the distance between vector a and vector b
        return Math.sqrt((b.x - a.x)*(b.x - a.x) + (b.y - a.y)*(b.y - a.y));
    }
}
