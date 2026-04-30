package com.oasisfeng.island.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;
import java.util.Random;

public class FakeIdentityManager {

    private static final String TAG = "FakeIdentityManager";
    private static final String PREF_NAME_PREFIX = "fake_identity_";

    public static final String KEY_ANDROID_ID = "ANDROID_ID";
    public static final String KEY_SERIAL = "SERIAL";
    public static final String KEY_MODEL = "MODEL";
    public static final String KEY_MANUFACTURER = "MANUFACTURER";
    public static final String KEY_FINGERPRINT = "FINGERPRINT";
    public static final String KEY_IMEI = "IMEI";
    public static final String KEY_SIM_SERIAL_NUMBER = "SIM_SERIAL_NUMBER";
    public static final String KEY_MAC_ADDRESS = "MAC_ADDRESS";

    private static SharedPreferences getPrefs(Context context, UserHandle user) {
        String prefName = PREF_NAME_PREFIX + Users.toId(user);
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    private static class DeviceTemplate {
        String manufacturer;
        String model;
        String fingerprint;

        DeviceTemplate(String manufacturer, String model, String fingerprint) {
            this.manufacturer = manufacturer;
            this.model = model;
            this.fingerprint = fingerprint;
        }
    }

    private static final DeviceTemplate[] DEVICE_TEMPLATES = new DeviceTemplate[]{
            // Samsung
            new DeviceTemplate("samsung", "SM-S928B", "samsung/e3qsqx/e3q:15/AP3A.241005.015/S928BXXS3AXI1:user/release-keys"), // S24 Ultra Android 15
            new DeviceTemplate("samsung", "SM-S938B", "samsung/e4qsqx/e4q:16/BP1A.250305.019/S938BXXS1BXI1:user/release-keys"), // S25 Ultra (Mock Android 16)
            new DeviceTemplate("samsung", "SM-S918B", "samsung/dm3qsqx/dm3q:14/UP1A.231005.007/S918BXXS3BWK5:user/release-keys"), // S23 Ultra
            // Google Pixel
            new DeviceTemplate("Google", "Pixel 9 Pro XL", "google/komodo/komodo:15/AP3A.241005.015/12345678:user/release-keys"), // Pixel 9 Pro XL Android 15
            new DeviceTemplate("Google", "Pixel 9 Pro", "google/caiman/caiman:16/BP1A.250305.019/87654321:user/release-keys"), // Pixel 9 Pro Android 16
            new DeviceTemplate("Google", "Pixel 8 Pro", "google/husky/husky:15/AP3A.241005.015/11223344:user/release-keys"),
            // Xiaomi
            new DeviceTemplate("Xiaomi", "24129PN74C", "Xiaomi/houji/houji:15/OS1.1.0.0.VNACNXM/24129:user/release-keys"), // Xiaomi 15 Pro
            new DeviceTemplate("Xiaomi", "23116PN5BC", "Xiaomi/shennong/shennong:14/UKQ1.230804.001/V816.0.10.0.UNCCNXM:user/release-keys"), // Xiaomi 14 Pro
            // Vivo
            new DeviceTemplate("vivo", "V2309A", "vivo/V2309A/V2309A:15/AP3A.241005.015/compiler1234:user/release-keys"), // Vivo X100 Pro
            // Oppo
            new DeviceTemplate("OPPO", "PHZ110", "OPPO/PHZ110/PHZ110:15/AP3A.241005.015/111111:user/release-keys"), // Find X8 Pro
            // Huawei (HarmonyOS/Android blend mock)
            new DeviceTemplate("HUAWEI", "HBN-AL80", "HUAWEI/HBN-AL80/HBN-AL80:12/HUAWEIHBN-AL80/104000300:user/release-keys") // Pura 70 Pro
            // Add more as needed...
    };

    public static void generateAndStoreIdentity(Context context, UserHandle user) {
        Log.i(TAG, "Generating new fake identity for user: " + Users.toId(user));
        SharedPreferences prefs = getPrefs(context, user);
        SharedPreferences.Editor editor = prefs.edit();

        Random random = new Random();
        DeviceTemplate template = DEVICE_TEMPLATES[random.nextInt(DEVICE_TEMPLATES.length)];

        editor.putString(KEY_ANDROID_ID, generateAndroidId());
        editor.putString(KEY_SERIAL, generateRandomString(12, true));
        editor.putString(KEY_MODEL, template.model);
        editor.putString(KEY_MANUFACTURER, template.manufacturer);
        editor.putString(KEY_FINGERPRINT, template.fingerprint);
        editor.putString(KEY_IMEI, generateImei());
        editor.putString(KEY_SIM_SERIAL_NUMBER, generateSimSerialNumber());
        editor.putString(KEY_MAC_ADDRESS, generateMacAddress());

        editor.apply();
    }

    public static void clearIdentity(Context context, UserHandle user) {
        Log.i(TAG, "Clearing fake identity for user: " + Users.toId(user));
        SharedPreferences prefs = getPrefs(context, user);
        prefs.edit().clear().apply();
        // Also delete the preferences file if possible, or just let it be cleared.
    }

    public static String getFakeIdentityValue(Context context, String key) {
        // By default, assume we are querying for the current user's fake identity
        return getFakeIdentityValue(context, android.os.Process.myUserHandle(), key);
    }

    public static String getFakeIdentityValue(Context context, UserHandle user, String key) {
        SharedPreferences prefs = getPrefs(context, user);
        return prefs.getString(key, null);
    }

    public static void applyIdentity(Context context, UserHandle user) {
        int userId = Users.toId(user);
        Log.i(TAG, "Applying fake identity for user: " + userId);

        SharedPreferences prefs = getPrefs(context, user);
        String androidId = prefs.getString(KEY_ANDROID_ID, null);
        String model = prefs.getString(KEY_MODEL, null);
        String manufacturer = prefs.getString(KEY_MANUFACTURER, null);
        String fingerprint = prefs.getString(KEY_FINGERPRINT, null);

        StringBuilder commands = new StringBuilder();

        // 1. Per-Profile Android ID
        if (androidId != null) {
            commands.append(String.format("settings put --user %d secure android_id %s\n", userId, androidId));
        }

        // 2. Global Properties via resetprop (Magisk)
        String serial = prefs.getString(KEY_SERIAL, null);
        if (serial != null) {
            commands.append(String.format("resetprop ro.serialno \"%s\"\n", serial));
            commands.append(String.format("resetprop ro.boot.serialno \"%s\"\n", serial));
        }
        if (model != null) {
            commands.append(String.format("resetprop ro.product.model \"%s\"\n", model));
            commands.append(String.format("resetprop ro.product.vendor.model \"%s\"\n", model));
        }
        if (manufacturer != null) {
            commands.append(String.format("resetprop ro.product.manufacturer \"%s\"\n", manufacturer));
            commands.append(String.format("resetprop ro.product.vendor.manufacturer \"%s\"\n", manufacturer));
        }
        if (fingerprint != null) {
            commands.append(String.format("resetprop ro.build.fingerprint \"%s\"\n", fingerprint));
            commands.append(String.format("resetprop ro.vendor.build.fingerprint \"%s\"\n", fingerprint));
        }

        // 3. Clear Google Play Services cache for this profile
        commands.append(String.format("pm clear --user %d com.google.android.gms\n", userId));

        // 4. Force Stop all third-party apps in this profile
        // Get all packages in the specific user profile, exclude system apps, and force stop them
        commands.append(String.format("for pkg in $(pm list packages -3 --user %d | cut -d':' -f2); do am force-stop --user %d $pkg; done\n", userId, userId));

        executeRootCommands(commands.toString());
    }

    private static void executeRootCommands(String commands) {
        try {
            Log.d(TAG, "Executing root commands:\n" + commands);
            Process process = Runtime.getRuntime().exec("su");
            java.io.DataOutputStream os = new java.io.DataOutputStream(process.getOutputStream());
            os.writeBytes(commands);
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            Log.d(TAG, "Root commands executed, exit value: " + process.exitValue());
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute root commands", e);
        }
    }

    private static String generateAndroidId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }

    private static String generateMacAddress() {
        Random random = new Random();
        byte[] macAddr = new byte[6];
        random.nextBytes(macAddr);
        macAddr[0] = (byte) (macAddr[0] & (byte) 254);  // zeroing last 2 bytes to make it unicast and globally unique
        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {
            if (sb.length() > 0) sb.append(":");
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String generateImei() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String generateSimSerialNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String generateRandomString(int length, boolean alphanumeric) {
        String chars = alphanumeric ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" : "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
