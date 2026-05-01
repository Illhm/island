package com.oasisfeng.island.provisioning.identity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import android.text.TextUtils;

/**
 * Helper class to generate and manage automated identity lifecycle in Island profiles.
 */
public class IdentityGenerator {

    private static final String TAG = "IdentityGenerator";
    private static final String PREF_NAME = "island_identity_prefs";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Preference Keys
    private static final String PREF_KEY_MODEL = "build_model";
    private static final String PREF_KEY_BUILD_ID = "build_id";
    private static final String PREF_KEY_ANDROID_ID = "android_id";
    private static final String PREF_KEY_FINGERPRINT = "build_fingerprint";
    private static final String PREF_KEY_GSF_ID = "gsf_id";

    // Templates for popular devices
    private static final List<DeviceTemplate> TEMPLATES = Arrays.asList(
            new DeviceTemplate("Pixel 9", "google/tokay/tokay:15/AP3A.241005.015/1234567:user/release-keys", "AP3A.241005.015"),
            new DeviceTemplate("Pixel 9 Pro", "google/caiman/caiman:15/AP3A.241005.015/1234567:user/release-keys", "AP3A.241005.015"),
            new DeviceTemplate("Pixel 9 Pro XL", "google/komodo/komodo:15/AP3A.241005.015/1234567:user/release-keys", "AP3A.241005.015"),
            new DeviceTemplate("Pixel 10", "google/pixel10/pixel10:16/BP1A.251005.010/1357924:user/release-keys", "BP1A.251005.010"),
            new DeviceTemplate("SM-S928B", "samsung/e3sxx/e3s:15/UP1A.231005.007/S928BXXU1AXB5:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("SM-S938B", "samsung/e4sxx/e4s:16/VP1A.241005.008/S938BXXU1AXC6:user/release-keys", "VP1A.241005.008"),
            new DeviceTemplate("Find X8", "Oppo/FindX8/FindX8:15/UP1A.231005.007/2405082119:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("Find X8 Pro", "Oppo/FindX8Pro/FindX8Pro:15/UP1A.231005.007/2405082120:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("V2309A", "vivo/PD2309/PD2309:15/UP1A.231005.007/2405082119:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("V2329A", "vivo/PD2329/PD2329:15/UP1A.231005.007/2405082120:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("Xiaomi 14", "Xiaomi/houji/houji:15/UP1A.231005.007/2405082119:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("Xiaomi 15 Ultra", "Xiaomi/aurora/aurora:16/VP1A.241005.008/2505082119:user/release-keys", "VP1A.241005.008"),
            new DeviceTemplate("Mate 60", "HUAWEI/ALN-AL00/ALN-AL00:15/UP1A.231005.007/2405082119:user/release-keys", "UP1A.231005.007"),
            new DeviceTemplate("Mate 70", "HUAWEI/Mate70-AL00/Mate70-AL00:16/VP1A.241005.008/2505082119:user/release-keys", "VP1A.241005.008")
    );

    public static void generateAndSaveIdentity(Context context) {
        DeviceTemplate template = TEMPLATES.get(RANDOM.nextInt(TEMPLATES.size()));

        String model = template.model;
        String buildId = template.baseBuildId + "." + generateRandomHex(6).toUpperCase();
        String fingerprint = modifyFingerprint(template.fingerprint, buildId);
        String androidId = generateRandomHex(16);
        String gsfId = generateRandomHex(16);

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PREF_KEY_MODEL, model)
                .putString(PREF_KEY_BUILD_ID, buildId)
                .putString(PREF_KEY_FINGERPRINT, fingerprint)
                .putString(PREF_KEY_ANDROID_ID, androidId)
                .putString(PREF_KEY_GSF_ID, gsfId)
                .apply();

        Log.i(TAG, "Generated new identity for profile: Model=" + model + ", Android ID=" + androidId);
    }

    public static void clearIdentity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.i(TAG, "Cleared identity preferences for profile.");
    }

    private static String generateRandomHex(int length) {
        byte[] bytes = new byte[(length + 1) / 2];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.substring(0, length);
    }

    private static String modifyFingerprint(String baseFingerprint, String newBuildId) {
        // Find the build ID block in the fingerprint, which is generally the 4th element separated by '/'
        String[] parts = baseFingerprint.split("/");
        if (parts.length > 3) {
            String oldBuildId = parts[3];
            return baseFingerprint.replace(oldBuildId, newBuildId);
        }
        return baseFingerprint;
    }

    public static String getActiveIdentityModel(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_MODEL, "Unknown");
    }

    public static String getActiveIdentityAndroidId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(PREF_KEY_ANDROID_ID, "Unknown");
    }

    private static class DeviceTemplate {
        String model;
        String fingerprint;
        String baseBuildId;

        DeviceTemplate(String model, String fingerprint, String baseBuildId) {
            this.model = model;
            this.fingerprint = fingerprint;
            this.baseBuildId = baseBuildId;
        }
    }
}
