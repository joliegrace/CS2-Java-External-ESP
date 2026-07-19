package com.cs2esp;

public class Vector3 {
    public float x, y, z;

    public Vector3() { this(0, 0, 0); }

    public Vector3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }

    public Vector3(Vector3 other) { this(other.x, other.y, other.z); }

    public Vector3 add(Vector3 o) { return new Vector3(x + o.x, y + o.y, z + o.z); }

    public Vector3 sub(Vector3 o) { return new Vector3(x - o.x, y - o.y, z - o.z); }

    public Vector3 mul(float s) { return new Vector3(x * s, y * s, z * s); }

    public Vector3 div(float s) { return s == 0 ? new Vector3(0, 0, 0) : new Vector3(x / s, y / s, z / s); }

    public float dot(Vector3 o) { return x * o.x + y * o.y + z * o.z; }

    public Vector3 cross(Vector3 o) {
        return new Vector3(
            y * o.z - z * o.y,
            z * o.x - x * o.z,
            x * o.y - y * o.x
        );
    }

    public float lengthSq() { return x * x + y * y + z * z; }

    public float length() { return (float) Math.sqrt(lengthSq()); }

    public float distanceTo(Vector3 o) { return sub(o).length(); }

    public float distanceSqTo(Vector3 o) { return sub(o).lengthSq(); }

    public float distance2DTo(Vector3 o) {
        float dx = x - o.x, dy = y - o.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public Vector3 normalize() {
        float len = length();
        return len == 0 ? new Vector3(0, 0, 0) : div(len);
    }

    public static Vector3 lerp(Vector3 a, Vector3 b, float t) {
        return new Vector3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        );
    }

    public boolean isZero() { return x == 0 && y == 0 && z == 0; }

    public Vector2 toVector2() { return new Vector2(x, y); }

    public float angleTo(Vector3 o) {
        float dotP = dot(o);
        float lenProduct = length() * o.length();
        if (lenProduct == 0) return 0;
        return (float) Math.acos(Math.max(-1f, Math.min(1f, dotP / lenProduct)));
    }

    @Override
    public String toString() { return String.format("(%.3f, %.3f, %.3f)", x, y, z); }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3)) return false;
        Vector3 o = (Vector3) obj;
        return Float.compare(x, o.x) == 0 && Float.compare(y, o.y) == 0 && Float.compare(z, o.z) == 0;
    }
}