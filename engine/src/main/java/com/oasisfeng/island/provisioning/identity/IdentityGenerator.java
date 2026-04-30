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
            new DeviceTemplate("Pixel 5", "google/redfin/redfin:11/RQ3A.210905.001/7511028:user/release-keys", "RQ3A.210905.001"),
            new DeviceTemplate("Pixel 6", "google/oriole/oriole:12/SD1A.210817.036/7805805:user/release-keys", "SD1A.210817.036"),
            new DeviceTemplate("SM-G991B", "samsung/o1sxx/o1s:11/RP1A.200720.012/G991BXXU3AUF6:user/release-keys", "RP1A.200720.012"),
            new DeviceTemplate("SM-G998B", "samsung/p3sxx/p3s:11/RP1A.200720.012/G998BXXU3AUF6:user/release-keys", "RP1A.200720.012"),
            new DeviceTemplate("OnePlus 9", "OnePlus/OnePlus9/OnePlus9:11/RKQ1.201105.002/2107082119:user/release-keys", "RKQ1.201105.002")
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
        // Simple modification: replace the build ID part in the fingerprint
        String[] parts = baseFingerprint.split("/");
        if (parts.length > 3) {
            String oldBuildId = parts[3];
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains(oldBuildId)) {
                     parts[i] = parts[i].replace(oldBuildId, newBuildId);
                }
            }
        }
        return TextUtils.join("/", parts);
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
