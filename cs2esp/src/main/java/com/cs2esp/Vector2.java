package com.cs2esp;

public class Vector2 {
    public float x, y;

    public Vector2() { this(0, 0); }

    public Vector2(float x, float y) { this.x = x; this.y = y; }

    public Vector2(Vector2 other) { this(other.x, other.y); }

    public Vector2 add(Vector2 o) { return new Vector2(x + o.x, y + o.y); }

    public Vector2 sub(Vector2 o) { return new Vector2(x - o.x, y - o.y); }

    public Vector2 mul(float s) { return new Vector2(x * s, y * s); }

    public Vector2 div(float s) { return s == 0 ? new Vector2(0, 0) : new Vector2(x / s, y / s); }

    public float dot(Vector2 o) { return x * o.x + y * o.y; }

    public float cross(Vector2 o) { return x * o.y - y * o.x; }

    public float lengthSq() { return x * x + y * y; }

    public float length() { return (float) Math.sqrt(lengthSq()); }

    public float distanceTo(Vector2 o) { return sub(o).length(); }

    public float distanceSqTo(Vector2 o) { return sub(o).lengthSq(); }

    public Vector2 normalize() {
        float len = length();
        return len == 0 ? new Vector2(0, 0) : div(len);
    }

    public static Vector2 lerp(Vector2 a, Vector2 b, float t) {
        return new Vector2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }

    public float angleTo(Vector2 o) {
        float dotP = dot(o);
        float lenProduct = length() * o.length();
        if (lenProduct == 0) return 0;
        return (float) Math.acos(Math.max(-1f, Math.min(1f, dotP / lenProduct)));
    }

    public boolean isZero() { return x == 0 && y == 0; }

    @Override
    public String toString() { return String.format("(%.3f, %.3f)", x, y); }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2)) return false;
        Vector2 o = (Vector2) obj;
        return Float.compare(x, o.x) == 0 && Float.compare(y, o.y) == 0;
    }
}
