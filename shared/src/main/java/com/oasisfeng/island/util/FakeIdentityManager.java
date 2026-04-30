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

    public static void generateAndStoreIdentity(Context context, UserHandle user) {
        Log.i(TAG, "Generating new fake identity for user: " + Users.toId(user));
        SharedPreferences prefs = getPrefs(context, user);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_ANDROID_ID, generateAndroidId());
        editor.putString(KEY_SERIAL, generateRandomString(10, true));
        editor.putString(KEY_MODEL, "Island Device " + generateRandomString(4, false));
        editor.putString(KEY_MANUFACTURER, "Island Manufacturer");
        editor.putString(KEY_FINGERPRINT, "Island/Device/Device:10/QQ3A.200805.001/" + generateRandomString(6, false) + ":user/release-keys");
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
        if (model != null) {
            commands.append(String.format("resetprop ro.product.model \"%s\"\n", model));
        }
        if (manufacturer != null) {
            commands.append(String.format("resetprop ro.product.manufacturer \"%s\"\n", manufacturer));
        }
        if (fingerprint != null) {
            commands.append(String.format("resetprop ro.build.fingerprint \"%s\"\n", fingerprint));
        }

        // 3. Clear Google Play Services cache for this profile
        commands.append(String.format("pm clear --user %d com.google.android.gms\n", userId));

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
