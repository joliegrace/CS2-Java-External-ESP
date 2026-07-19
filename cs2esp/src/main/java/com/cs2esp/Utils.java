package com.cs2esp;

public class Utils {

    public static Vector3 worldToScreen(Vector3 worldPos, float[] matrix, int screenWidth, int screenHeight) {
        float x = matrix[0] * worldPos.x + matrix[1] * worldPos.y + matrix[2] * worldPos.z + matrix[3];
        float y = matrix[4] * worldPos.x + matrix[5] * worldPos.y + matrix[6] * worldPos.z + matrix[7];
        float w = matrix[12] * worldPos.x + matrix[13] * worldPos.y + matrix[14] * worldPos.z + matrix[15];
        if (w < 0.001f) return null; // Behind the camera
        float invW = 1.0f / w;
        x *= invW;
        y *= invW;
        float screenX = (screenWidth / 2f) + (x * screenWidth / 2f);
        float screenY = (screenHeight / 2f) - (y * screenHeight / 2f);
        return new Vector3(screenX, screenY, 0);
    }
}
