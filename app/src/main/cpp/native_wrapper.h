//
// Created by Ysard on 04/06/21.
//
// Note: not a C function but without extern C, JNA doesn't find it...

#ifndef unrooted_android_H
#define unrooted_android_H

#ifdef __cplusplus
extern "C" {
#endif

extern TiqiaaUsbIr* init_tiqiaa_wrapper(int fileDescriptor);
extern int transmit(TiqiaaUsbIr *tiqiaaDevice, int frequency, int *pulseEntry, int pulseLength);
extern int close_tiqiaa_wrapper(TiqiaaUsbIr * tiqiaaDevice);

#ifdef __cplusplus
}
#endif

#endif