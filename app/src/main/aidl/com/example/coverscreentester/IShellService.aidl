package com.example.coverscreentester;

interface IShellService {
    void injectMouse(int action, float x, float y, int displayId, int source, int buttonState, long downTime);
    void injectScroll(float x, float y, float vDistance, float hDistance, int displayId);
    void execClick(float x, float y, int displayId);
    void execRightClick(float x, float y, int displayId);
    void injectKey(int keyCode, int action);
    
    // New Window Management Methods
    void setWindowingMode(int taskId, int mode);
    void resizeTask(int taskId, int left, int top, int right, int bottom);
    String runCommand(String cmd);
    
    // NEW: Key injection on specific display
    void injectKeyOnDisplay(int keyCode, int action, int displayId);
}
