package com.example.coverscreentester;

interface IShellService {
    String runCommand(String cmd);
    void injectTouch(int action, float x, float y, int displayId);
    
    void execClick(float x, float y, int displayId);
    void execRightClick(float x, float y, int displayId);

    void injectMouse(int action, float x, float y, int displayId, int source, int buttonState, long downTime);

    void execKey(int keyCode);

    // 🚨 UPDATED: Supports Vertical AND Horizontal Scroll
    void injectScroll(float x, float y, float vDistance, float hDistance, int displayId);
}
