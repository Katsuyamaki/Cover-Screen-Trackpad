package com.example.coverscreentester;

interface IShellService {
    String runCommand(String cmd);
    void injectTouch(int action, float x, float y, int displayId);
    
    void execClick(float x, float y, int displayId);
    void execRightClick(float x, float y, int displayId);

    void injectMouse(int action, float x, float y, int displayId, int source, int buttonState, long downTime);

    // 🚨 NEW: Generic key injection (for Voice, Back, Home, etc.)
    void execKey(int keyCode);
}
