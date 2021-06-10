package com.redirectapps.tvkill;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface nativeWrapper extends Library {
    public static final nativeWrapper INSTANCE = Native.load("native_wrapper", nativeWrapper.class);

    public Pointer init_tiqiaa_wrapper(int fileDescriptor);
    public int transmit(Pointer tiqiaaDevice, int frequency, int[] pulseEntry, int pulseLength);
    public int close_tiqiaa_wrapper(Pointer tiqiaaDevice);
}