LOCAL_PATH := $(call my-dir)
NATIVELIB_ROOT_REL :=  $(LOCAL_PATH)/../cpp
# TVKILL/app/src/main/jni/../cpp

# Libusb static build
include $(LOCAL_PATH)/libusb/android/jni/libusb.mk


# Tiqiaa

include $(CLEAR_VARS)

LOCAL_CFLAGS := -pthread
LOCAL_SRC_FILES := $(NATIVELIB_ROOT_REL)/TiqiaaUsb.cpp

LOCAL_STATIC_LIBRARIES := usb1.0
LOCAL_LDLIBS += -llog -lpthread
LOCAL_MODULE := tiqiaa

include $(BUILD_STATIC_LIBRARY)



# Native Wrapper

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(NATIVELIB_ROOT_REL)/native_wrapper.cpp

LOCAL_STATIC_LIBRARIES := tiqiaa
LOCAL_LDLIBS += -llog
LOCAL_MODULE := native_wrapper

override include $(BUILD_SHARED_LIBRARY)
