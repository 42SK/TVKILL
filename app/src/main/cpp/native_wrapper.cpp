//
// Created by Ysard on 04/06/21.
//

#include "TiqiaaUsb.h"
#include "native_wrapper.h"
#include <android/log.h>
#define  LOG_TAG    "TVKILL-Native"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


TiqiaaUsbIr * init_tiqiaa_wrapper(int fileDescriptor) {
    // Init USB device through libusb and init Tiqiaa wrapper
    libusb_context *ctx = NULL;
    libusb_device_handle *devh = NULL;
    int r = 0;
    LOGD("Init!");
    r = libusb_set_option(ctx, LIBUSB_OPTION_WEAK_AUTHORITY, NULL);
    if (r != LIBUSB_SUCCESS) {
        LOGD("libusb_set_option failed: %d\n", r);
        return NULL;
    }
    LOGD("libusb_set_option OK: %d\n", r);
    r = libusb_init(&ctx);
    if (r < 0) {
        LOGD("libusb_init failed: %d\n", r);
        return NULL;
    }
    LOGD("libusb_init OK: %d\n", r);
    r = libusb_wrap_sys_device(ctx, (intptr_t)fileDescriptor, &devh);
    if (r < 0) {
        LOGD("libusb_wrap_sys_device failed: %d\n", r);
        return NULL;
    } else if (devh == NULL) {
        LOGD("libusb_wrap_sys_device returned invalid handle\n");
        return NULL;
    }
    LOGD("libusb_wrap_sys_device OK: %d\n", r);

    // Init TiqiaaUsbIr pointer
    TiqiaaUsbIr *Ir = new TiqiaaUsbIr();
    bool ret = Ir->Open(libusb_get_device(devh), devh);
    if (!ret) {
        LOGD("Tiqiaa error; Device not found?");
        return NULL;
    }
    return Ir;
}

int transmit(TiqiaaUsbIr *tiqiaaDevice, int frequency, int *pulseEntry, int pulseLength) {
    // Transmit the array of pulses
    bool ret = tiqiaaDevice->SendPulseSignal(frequency, pulseEntry, pulseLength);
    if (!ret) {
        LOGD("Tiqiaa transmit Error!");
        return -1;
    }
    return 0;
}

int close_tiqiaa_wrapper(TiqiaaUsbIr * tiqiaaDevice) {
    // Close device usb device and free memory
    bool ret = tiqiaaDevice->Close();
    if (!ret) {
        LOGD("Tiqiaa close ERROR!");
        return -1;
    }
    delete tiqiaaDevice;
    return 0;
}
