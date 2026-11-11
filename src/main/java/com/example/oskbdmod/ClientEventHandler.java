java
package com.example.oskbdmod;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.io.IOException;
import java.lang.reflect.Method;

public class ClientEventHandler {
    private Object lastFocusedElement = null;
    private static final int SW_MINIMIZE = 6;
    private static final int SW_RESTORE = 9;

    @SubscribeEvent
    public void onScreenTick(ScreenEvent.Opening event) {
        lastFocusedElement = null;
        dismissOSK();
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) {
            if (lastFocusedElement != null) {
                lastFocusedElement = null;
                dismissOSK();
            }
            return;
        }

        GuiEventListener focused = screen.getFocused();
        if (focused == null) {
            if (lastFocusedElement != null) {
                lastFocusedElement = null;
                dismissOSK();
            }
            return;
        }

        if (focused instanceof EditBox currentBox) {
            if (currentBox.isEditable() && currentBox.isVisible()) {
                if (currentBox != lastFocusedElement) {
                    lastFocusedElement = currentBox;
                    triggerWindowsOSK();
                }
            } else if (lastFocusedElement == currentBox) {
                lastFocusedElement = null;
                dismissOSK();
            }
            return;
        }

        String className = focused.getClass().getName().toLowerCase();
        if (isTargetInputClass(className)) {
            if (focused != lastFocusedElement) {
                if (checkFuzzyFocusedState(focused)) {
                    lastFocusedElement = focused;
                    triggerWindowsOSK();
                }
            }
        } else {
            if (lastFocusedElement != null) {
                lastFocusedElement = null;
                dismissOSK();
            }
        }
    }

    private boolean isTargetInputClass(String className) {
        return className.contains("mezz.jei") 
            || className.contains("appeng")   
            || className.contains("ftbquests") 
            || className.contains("custommachinery") 
            || className.contains("toms_storage")
            || className.contains("textfield") 
            || className.contains("editbox") 
            || className.contains("searchfield") 
            || className.contains("filterfield");
    }

    private boolean checkFuzzyFocusedState(Object element) {
        String methodNames = {"isFocused", "m_93696_", "isFocusedField", "canWrite"};
        for (String methodName : methodNames) {
            try {
                Method method = element.getClass().getMethod(methodName);
                if (method.getReturnType() == boolean.class) {
                    return (boolean) method.invoke(element);
                }
            } catch (Exception ignored) {}
        }
        return true;
    }

    private void triggerWindowsOSK() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        HWND hwnd = User32.INSTANCE.FindWindow("OSKMainClass", null);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, SW_RESTORE);
        } else {
            try {
                new ProcessBuilder("cmd.exe", "/c", "start", "osk.exe").start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dismissOSK() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        HWND hwnd = User32.INSTANCE.FindWindow("OSKMainClass", null);
        if (hwnd != null) {
            HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
            if (foregroundWindow != null && foregroundWindow.equals(hwnd)) {
                return;
            }
            User32.INSTANCE.ShowWindow(hwnd, SW_MINIMIZE);
        }
    }
}