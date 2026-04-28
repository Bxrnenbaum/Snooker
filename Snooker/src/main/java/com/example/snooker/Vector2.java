package com.example.snooker;

public class Vector2 {
    double x;
    double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 sum(Vector2 b) { //returns the sum of vector this and vector b
        return new Vector2(this.x + b.x, this.y + b.y);
    }

    public Vector2 difference(Vector2 b) { //returns the difference between vector this and vector b
        return new Vector2(this.x - b.x, this.y - b.y);
    }

    public Vector2 abs() { //returns the absolute of the vector
        return new Vector2(Math.abs(this.x), Math.abs(this.y));
    }

    public Vector2 normalize() { //returns the normalized vector
        double magnitude = Math.sqrt(this.x * this.x + this.y * this.y);
        if (magnitude == 0)
            return new Vector2(0, 0);

        return new Vector2(this.x / magnitude, this.y / magnitude);
    }

    public double dot(Vector2 b) { //returns the dot product of the vectors
        return this.x * b.x + this.y * b.y;
    }

    public Vector2 scalar(double scalar) { // returns this vector where each component has been multiplied by the scalar
        return new Vector2(scalar * this.x, scalar * this.y);
    }

    public double distance(Vector2 b) { //returns the distance between vector this and vector b
        return Math.sqrt((b.x - this.x) * (b.x - this.x) + (b.y - this.y) * (b.y - this.y));
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
}
