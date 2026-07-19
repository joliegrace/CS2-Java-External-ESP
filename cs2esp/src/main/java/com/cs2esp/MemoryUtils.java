package com.cs2esp;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.Tlhelp32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MemoryUtils {

    public interface Kernel32Ext extends com.sun.jna.win32.StdCallLibrary {
        Kernel32Ext INSTANCE = Native.load("kernel32", Kernel32Ext.class, W32APIOptions.DEFAULT_OPTIONS);
        HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
        boolean ReadProcessMemory(HANDLE hProcess, Pointer lpBaseAddress, byte[] lpBuffer, int nSize, int[] lpNumberOfBytesRead);
        boolean WriteProcessMemory(HANDLE hProcess, Pointer lpBaseAddress, byte[] lpBuffer, int nSize, int[] lpNumberOfBytesWritten);
        boolean CloseHandle(HANDLE hObject);
    }

    private static final int PROCESS_ALL_ACCESS = 0x1F0FFF;

    private HANDLE processHandle;
    private int pid;
    private long moduleBase = -1;

    public boolean open(String processName) {
        pid = findProcessId(processName);
        if (pid == 0) {
            return false;
        }
        processHandle = Kernel32Ext.INSTANCE.OpenProcess(PROCESS_ALL_ACCESS, false, pid);
        if (processHandle == null) {
            return false;
        }
        moduleBase = getModuleBase(pid, processName);
        return true;
    }

    public void close() {
        if (processHandle != null) {
            Kernel32Ext.INSTANCE.CloseHandle(processHandle);
        }
    }

    public long getModuleBase() {
        return moduleBase;
    }

    int findProcessId(String processName) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new com.sun.jna.platform.win32.WinDef.DWORD(0));
        PROCESSENTRY32.ByReference entry = new PROCESSENTRY32.ByReference();
        int result = 0;
        try {
            if (Kernel32.INSTANCE.Process32First(snapshot, entry)) {
                do {
                    String exe = Native.toString(entry.szExeFile);
                    if (exe.equalsIgnoreCase(processName)) {
                        result = entry.th32ProcessID.intValue();
                        break;
                    }
                } while (Kernel32.INSTANCE.Process32Next(snapshot, entry));
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return result;
    }

    long getModuleBase(int pid, String moduleName) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPMODULE, new com.sun.jna.platform.win32.WinDef.DWORD(pid));
        MODULEENTRY32W.ByReference entry = new MODULEENTRY32W.ByReference();
        long base = -1;
        try {
            if (Kernel32.INSTANCE.Module32FirstW(snapshot, entry)) {
                do {
                    String modName = Native.toString(entry.szModule);
                    if (modName.equalsIgnoreCase(moduleName)) {
                        base = Pointer.nativeValue(entry.modBaseAddr);
                        break;
                    }
                } while (Kernel32.INSTANCE.Module32NextW(snapshot, entry));
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return base;
    }

    HWND getWindowHandle (int processID) {
        HWND[] foundWindow = new HWND[1];
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
            if (pid.getValue() == processID) {
                foundWindow[0] = hwnd;
                return false;
            }
            return true;
        }, null);
        return foundWindow[0];
    }

    public byte[] readBytes(long address, int size) {
        byte[] buffer = new byte[size];
        int[] bytesRead = new int[1];
        boolean ok = Kernel32Ext.INSTANCE.ReadProcessMemory(processHandle, new Pointer(address), buffer, size, bytesRead);
        return ok ? buffer : null;
    }

    public boolean writeBytes(long address, byte[] data) {
        int[] bytesWritten = new int[1];
        return Kernel32Ext.INSTANCE.WriteProcessMemory(processHandle, new Pointer(address), data, data.length, bytesWritten);
    }

    public int readInt(long address) {
        byte[] b = readBytes(address, 4);
        return b == null ? 0 : ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public float readFloat(long address) {
        byte[] b = readBytes(address, 4);
        return b == null ? 0f : ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public long readLong(long address) {
        byte[] b = readBytes(address, 8);
        return b == null ? 0L : ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public String readString(long address, int maxLength) {
        byte[] buffer = readBytes(address, maxLength);
        if (buffer == null) return null;

        int len = 0;
        while (len < buffer.length && buffer[len] != 0) len++;

        return new String(buffer, 0, len, java.nio.charset.StandardCharsets.UTF_8);
    }

    public String readStringUnicode(long address, int maxLength) {
        byte[] buffer = readBytes(address, maxLength * 2);
        if (buffer == null) return null;

        int len = 0;
        while (len < maxLength - 1) {
            int idx = len * 2;
            if (buffer[idx] == 0 && buffer[idx + 1] == 0) break;
            len++;
        }
        return new String(buffer, 0, len * 2, java.nio.charset.StandardCharsets.UTF_16LE);
    }

    public long readPointer(long address) {
        return readLong(address);
    }

    public Vector3 readVector3(long address) {
        byte[] b = readBytes(address, 12);
        if (b == null) return null;
        ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        return new Vector3(buf.getFloat(0), buf.getFloat(4), buf.getFloat(8));
    }

    public Vector2 readVector2(long address) {
        byte[] b = readBytes(address, 8);
        if (b == null) return null;
        ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        return new Vector2(buf.getFloat(0), buf.getFloat(4));
    }

    public float[] readMatrix4x4(long address) {
        byte[] b = readBytes(address, 64);
        if (b == null) return null;
        ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        float[] m = new float[16];
        for (int i = 0; i < 16; i++) {
            m[i] = buf.getFloat(i * 4);
        }
        return m;
    }

    public void writeInt(long address, int value) {
        byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        writeBytes(address, b);
    }

    public void writeFloat(long address, float value) {
        byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
        writeBytes(address, b);
    }

    public long resolvePointerChain(long baseAddress, long[] offsets) {
        long addr = baseAddress;
        for (int i = 0; i < offsets.length; i++) {
            addr = readPointer(addr) + offsets[i];
        }
        return addr;
    }
}