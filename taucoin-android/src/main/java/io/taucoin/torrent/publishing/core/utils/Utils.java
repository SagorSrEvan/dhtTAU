package io.taucoin.torrent.publishing.core.utils;

import android.content.Context;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import io.taucoin.torrent.publishing.MainApplication;
import io.taucoin.torrent.publishing.R;
import io.taucoin.torrent.publishing.core.settings.SettingsRepository;
import io.taucoin.torrent.publishing.core.storage.sqlite.RepositoryHelper;
import io.taucoin.torrent.publishing.receiver.BootReceiver;

import java.io.File;
import java.net.IDN;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/*
 * General utils.
 */

public class Utils
{
    public static final String INFINITY_SYMBOL = "\u221e";
    public static final String MAGNET_PREFIX = "magnet";
    public static final String HTTP_PREFIX = "http";
    public static final String HTTPS_PREFIX = "https";
    public static final String UDP_PREFIX = "udp";
    public static final String INFOHASH_PREFIX = "magnet:?xt=urn:btih:";
    public static final String FILE_PREFIX = "file";
    public static final String CONTENT_PREFIX = "content";
    public static final String TRACKER_URL_PATTERN =
            "^(https?|udp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String HASH_PATTERN = "\\b[0-9a-fA-F]{5,40}\\b";
    public static final String MIME_TORRENT = "application/x-bittorrent";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String NEWLINE_PATTERN = "\\r\\n|\\r|\\n";

    /*
     * Colorize the progress bar in the accent color (for pre-Lollipop).
     */

    public static void colorizeProgressBar(@NonNull Context context,
                                           @NonNull ProgressBar progress)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return;

        progress.getProgressDrawable().setColorFilter(getAttributeColor(context, R.attr.colorSecondary),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    /*
     * Returns the list of BencodeFileItem objects, extracted from FileStorage.
     * The order of addition in the list corresponds to the order of indexes in libtorrent4j.FileStorage
     */

//    public static ArrayList<BencodeFileItem> getFileList(@NonNull FileStorage storage)
//    {
//        ArrayList<BencodeFileItem> files = new ArrayList<>();
//        for (int i = 0; i < storage.numFiles(); i++) {
//            BencodeFileItem file = new BencodeFileItem(storage.filePath(i), i, storage.fileSize(i));
//            files.add(file);
//        }
//
//        return files;
//    }

    public static void setBackground(@NonNull View v,
                                     @NonNull Drawable d)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            v.setBackgroundDrawable(d);
        else
            v.setBackground(d);
    }

//    public static boolean checkConnectivity(@NonNull Context context)
//    {
//        SystemFacade systemFacade = SystemFacadeHelper.getSystemFacade(context);
//        NetworkInfo netInfo = systemFacade.getActiveNetworkInfo();
//
//        return netInfo != null && netInfo.isConnected() && isNetworkTypeAllowed(context);
//    }

//    public static boolean isNetworkTypeAllowed(@NonNull Context context)
//    {
//        SystemFacade systemFacade = SystemFacadeHelper.getSystemFacade(context);
//
//        SettingsRepository pref = RepositoryHelper.getSettingsRepository(context);
//        boolean enableRoaming = pref.enableRoaming();
//        boolean unmeteredOnly = pref.unmeteredConnectionsOnly();
//
//        boolean noUnmeteredOnly;
//        boolean noRoaming;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            NetworkCapabilities caps = systemFacade.getNetworkCapabilities();
//            /*
//             * Use ConnectivityManager#isActiveNetworkMetered() instead of NetworkCapabilities#NET_CAPABILITY_NOT_METERED,
//             * since Android detection VPN as metered, including on Android 9, oddly enough.
//             * I think this is due to what VPN services doesn't use setUnderlyingNetworks() method.
//             *
//             * See for details: https://developer.android.com/about/versions/pie/android-9.0-changes-all#network-capabilities-vpn
//             */
//            boolean unmetered = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
//                    !systemFacade.isActiveNetworkMetered();
//            noUnmeteredOnly = !unmeteredOnly || unmetered;
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                noRoaming = !enableRoaming || caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
//            } else {
//                NetworkInfo netInfo = systemFacade.getActiveNetworkInfo();
//                noRoaming = netInfo != null && !(enableRoaming && netInfo.isRoaming());
//            }
//
//        } else {
//            NetworkInfo netInfo = systemFacade.getActiveNetworkInfo();
//            if (netInfo == null) {
//                noUnmeteredOnly = false;
//                noRoaming = false;
//            } else {
//                noUnmeteredOnly = !unmeteredOnly || !systemFacade.isActiveNetworkMetered();
//                noRoaming = !(enableRoaming && netInfo.isRoaming());
//            }
//        }
//
//        return noUnmeteredOnly && noRoaming;
//    }

//    public static boolean isMetered(@NonNull Context context)
//    {
//        SystemFacade systemFacade = SystemFacadeHelper.getSystemFacade(context);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            NetworkCapabilities caps = systemFacade.getNetworkCapabilities();
//            return caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
//                    systemFacade.isActiveNetworkMetered();
//        } else {
//            return systemFacade.isActiveNetworkMetered();
//        }
//    }
//
//    public static boolean isRoaming(@NonNull Context context) {
//        SystemFacade systemFacade = SystemFacadeHelper.getSystemFacade(context);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            NetworkCapabilities caps = systemFacade.getNetworkCapabilities();
//            return caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
//        } else {
//            NetworkInfo netInfo = systemFacade.getActiveNetworkInfo();
//            return netInfo != null && netInfo.isRoaming();
//        }
//    }

    /*
     * Returns the link as "(http[s]|ftp)://[www.]name.domain/...".
     */

    public static String normalizeURL(@NonNull String url) {
        url = IDN.toUnicode(url);

        if (!url.startsWith(HTTP_PREFIX) && !url.startsWith(HTTPS_PREFIX))
            return HTTP_PREFIX + url;
        else
            return url;
    }

    /*
     * Returns the link as "magnet:?xt=urn:btih:hash".
     */

    public static String normalizeMagnetHash(@NonNull String hash)
    {
        return INFOHASH_PREFIX + hash;
    }

    /*
     * Don't use app context (its doesn't reload after configuration changes)
     */

    public static boolean isTwoPane(@NonNull Context context)
    {
        return context.getResources().getBoolean(R.bool.isTwoPane);
    }

    /*
     * Tablets (from 7"), notebooks, TVs
     *
     * Don't use app context (its doesn't reload after configuration changes)
     */

    public static boolean isLargeScreenDevice(@NonNull Context context)
    {
        return context.getResources().getBoolean(R.bool.isLargeScreenDevice);
    }

    /*
     * Returns true if link has the form "http[s][udp]://[www.]name.domain/...".
     *
     * Returns false if the link is not valid.
     */

    public static boolean isValidTrackerUrl(@NonNull String url)
    {
        if (TextUtils.isEmpty(url))
            return false;

        Pattern pattern = Pattern.compile(TRACKER_URL_PATTERN);
        Matcher matcher = pattern.matcher(url.trim());

        return matcher.matches();
    }

    public static boolean isHash(@NonNull String hash) {
        if (TextUtils.isEmpty(hash))
            return false;

        Pattern pattern = Pattern.compile(HASH_PATTERN);
        Matcher matcher = pattern.matcher(hash.trim());

        return matcher.matches();
    }

    /*
     * Return system text line separator (in android it '\n').
     */

    public static String getLineSeparator()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return System.lineSeparator();
        else
            return System.getProperty("line.separator");
    }

    @Nullable
    public static ClipData getClipData(@NonNull Context context)
    {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Activity.CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip())
            return null;

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0)
            return null;

        return clip;
    }

    public static List<CharSequence> getClipboardText(@NonNull Context context)
    {
        ArrayList<CharSequence> clipboardText = new ArrayList<>();

        ClipData clip = Utils.getClipData(context);
        if (clip == null)
            return clipboardText;

        for (int i = 0; i < clip.getItemCount(); i++) {
            CharSequence item = clip.getItemAt(i).getText();
            if (item == null)
                continue;
            clipboardText.add(item);
        }

        return clipboardText;
    }

//    public static void reportError(@NonNull Throwable error,
//                                   String comment)
//    {
//        if (comment != null)
//            ACRA.getErrorReporter().putCustomData(ReportField.USER_COMMENT.toString(), comment);
//
//        ACRA.getErrorReporter().handleSilentException(error);
//    }

    public static int dpToPx(@NonNull Context context, float dp)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }

    public static int getDefaultBatteryLowLevel() {
        return Resources.getSystem().getInteger(
                Resources.getSystem().getIdentifier("config_lowBatteryWarningLevel", "integer", "android"));
    }

    public static float getBatteryLevel(@NonNull Context context)
    {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null)
            return 50.0f;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        /* Error checking that probably isn't needed but I added just in case */
        if (level == -1 || scale == -1)
            return 50.0f;

        return ((float)level / (float)scale) * 100.0f;
    }

    public static boolean isBatteryCharging(@NonNull Context context)
    {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null)
            return false;
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public static boolean isBatteryLow(@NonNull Context context)
    {
        return Utils.getBatteryLevel(context) <= Utils.getDefaultBatteryLowLevel();
    }

    public static boolean isBatteryBelowThreshold(@NonNull Context context, int threshold)
    {
        return Utils.getBatteryLevel(context) <= threshold;
    }

//    public static int getThemePreference(@NonNull Context context)
//    {
//        return RepositoryHelper.getSettingsRepository(context).theme();
//    }

    public static int getAppTheme(@NonNull Context context)
    {
//        int theme = getThemePreference(context);
//
//        if (theme == Integer.parseInt(context.getString(R.string.pref_theme_light_value)))
//            return R.style.AppTheme;
//        else if (theme == Integer.parseInt(context.getString(R.string.pref_theme_dark_value)))
//            return R.style.AppTheme_Dark;
//        else if (theme == Integer.parseInt(context.getString(R.string.pref_theme_black_value)))
//            return R.style.AppTheme_Black;

        return R.style.AppTheme;
    }

    public static int getTranslucentAppTheme(@NonNull Context appContext)
    {
//        int theme = getThemePreference(appContext);
//
//        if (theme == Integer.parseInt(appContext.getString(R.string.pref_theme_light_value)))
//            return R.style.AppTheme_Translucent;
//        else if (theme == Integer.parseInt(appContext.getString(R.string.pref_theme_dark_value)))
//            return R.style.AppTheme_Translucent_Dark;
//        else if (theme == Integer.parseInt(appContext.getString(R.string.pref_theme_black_value)))
//            return R.style.AppTheme_Translucent_Black;

        return R.style.AppTheme_Translucent;
    }

//    public static int getSettingsTheme(@NonNull Context context)
//    {
//        int theme = getThemePreference(context);
//
//        if (theme == Integer.parseInt(context.getString(R.string.pref_theme_light_value)))
//            return R.style.AppTheme_Settings;
//        else if (theme == Integer.parseInt(context.getString(R.string.pref_theme_dark_value)))
//            return R.style.AppTheme_Settings_Dark;
//        else if (theme == Integer.parseInt(context.getString(R.string.pref_theme_black_value)))
//            return R.style.AppTheme_Settings_Black;
//
//        return R.style.AppTheme_Settings;
//    }

    public static boolean checkStoragePermission(@NonNull Context context)
    {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /*
     * Migrate from Tray settings database to shared preferences.
     * TODO: delete after some releases
     */
    @Deprecated
    public static void migrateTray2SharedPreferences(@NonNull Context appContext)
    {
        final String TAG = "tray2shared";
        final String migrate_key = "tray2shared_migrated";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(appContext);

        if (pref.getBoolean(migrate_key, false))
            return;

        File dbFile = appContext.getDatabasePath("tray.db");
        if (dbFile == null || !dbFile.exists()) {
            Log.w(TAG, "Database not found");
            pref.edit().putBoolean(migrate_key, true).apply();

            return;
        }
        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't open database: " + Log.getStackTraceString(e));
            appContext.deleteDatabase("tray");
            pref.edit().putBoolean(migrate_key, true).apply();

            return;
        }
        Cursor c = db.query("TrayPreferences",
                new String[]{"KEY", "VALUE"},
                null,
                null,
                null,
                null,
                null);
        SharedPreferences.Editor edit = pref.edit();
        Log.i(TAG, "Start migrate");
        try {
            int key_i = c.getColumnIndex("KEY");
            int value_i = c.getColumnIndex("VALUE");
            while (c.moveToNext()) {
                String key = c.getString(key_i);
                String value = c.getString(value_i);

                if (value.equalsIgnoreCase("true")) {
                    edit.putBoolean(key, true);
                } else if (value.equalsIgnoreCase("false")) {
                    edit.putBoolean(key, false);
                } else {
                    try {
                        int number = Integer.parseInt(value);
                        edit.putInt(key, number);
                    } catch (NumberFormatException e) {
                        edit.putString(key, value);
                    }
                }
            }
            Log.i(TAG, "Migrate completed");

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            c.close();
            db.close();
            appContext.deleteDatabase("tray.db");
            edit.putBoolean(migrate_key, true);
            edit.apply();
        }
    }

    /*
     * Workaround for start service in Android 8+ if app no started.
     * We have a window of time to get around to calling startForeground() before we get ANR,
     * if work is longer than a millisecond but less than a few seconds.
     */

    public static void startServiceBackground(@NonNull Context context, @NonNull Intent i)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(i);
        else
            context.startService(i);
    }

    /**
     * 启动/禁止设备启动广播接收器
     * @param context 上下文
     * @param enable 是否启动
     */
    public static void enableBootReceiver(@NonNull Context context, boolean enable) {
        int flag = !enable ?
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        ComponentName bootReceiver = new ComponentName(context, BootReceiver.class);
        context.getPackageManager()
                .setComponentEnabledSetting(bootReceiver, flag, PackageManager.DONT_KILL_APP);
    }


    /*
     * Without additional information (e.g -DEBUG)
     */

    public static String getAppVersionNumber(@NonNull String versionName)
    {
        int index = versionName.indexOf("-");
        if (index >= 0)
            versionName = versionName.substring(0, index);

        return versionName;
    }

    /*
     * Return version components in these format: [major, minor, revision]
     */

    public static int[] getVersionComponents(@NonNull String versionName)
    {
        int[] version = new int[3];

        /* Discard additional information */
        versionName = getAppVersionNumber(versionName);

        String[] components = versionName.split("\\.");
        if (components.length < 2)
            return version;

        try {
            version[0] = Integer.parseInt(components[0]);
            version[1] = Integer.parseInt(components[1]);
            if (components.length >= 3)
                version[2] = Integer.parseInt(components[2]);

        } catch (NumberFormatException e) {
            /* Ignore */
        }

        return version;
    }

    public static String makeSha1Hash(@NonNull String s)
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        messageDigest.update(s.getBytes(Charset.forName("UTF-8")));
        StringBuilder sha1 = new StringBuilder();
        for (byte b : messageDigest.digest()) {
            if ((0xff & b) < 0x10)
                sha1.append("0");
            sha1.append(Integer.toHexString(0xff & b));
        }

        return sha1.toString();
    }

    public static SSLContext getSSLContext() throws GeneralSecurityException
    {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore)null);

        TrustManager[] trustManagers = tmf.getTrustManagers();
        final X509TrustManager origTrustManager = (X509TrustManager)trustManagers[0];

        TrustManager[] wrappedTrustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return origTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException
                    {
                        origTrustManager.checkClientTrusted(certs, authType);
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException
                    {
                        origTrustManager.checkServerTrusted(certs, authType);
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext;
    }

    public static void showActionModeStatusBar(@NonNull Activity activity, boolean mode)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        int attr = (mode ? R.attr.actionModeBackground : R.attr.statusBarColor);
        activity.getWindow().setStatusBarColor(getAttributeColor(activity, attr));
    }

    public static int getAttributeColor(@NonNull Context context, int attributeId)
    {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);

        } catch (Resources.NotFoundException e) {
            return color;
        }

        return color;
    }

    /*
     * Return path to the current torrent download directory.
     * If the directory doesn't exist, the function creates it automatically
     */

//    public static Uri getTorrentDownloadPath(@NonNull Context appContext)
//    {
//        SettingsRepository pref = RepositoryHelper.getSettingsRepository(appContext);
//        String path = pref.saveTorrentsIn();
//
//        FileSystemFacade fs = SystemFacadeHelper.getFileSystemFacade(appContext);
//
//        path = (TextUtils.isEmpty(path) ? fs.getDefaultDownloadPath() : path);
//
//        return (path == null ? null : Uri.parse(fs.normalizeFileSystemPath(path)));
//    }

    public static void setTextViewStyle(@NonNull Context context,
                                        @NonNull TextView textView,
                                        int resId)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            textView.setTextAppearance(context, resId);
        else
            textView.setTextAppearance(resId);
    }

//    public static boolean isSafPath(@NonNull Context appContext, @NonNull Uri path)
//    {
//        return SafFileSystem.getInstance(appContext).isSafPath(path);
//    }

    public static boolean isFileSystemPath(@NonNull Uri path)
    {
        String scheme = path.getScheme();
        if (scheme == null)
            throw new IllegalArgumentException("Scheme of " + path.getPath() + " is null");

        return scheme.equals(ContentResolver.SCHEME_FILE);
    }

//    public static List<DrawerGroup> getNavigationDrawerItems(@NonNull Context context,
//                                                             @NonNull SharedPreferences localPref)
//    {
//        Resources res = context.getResources();
//
//        ArrayList<DrawerGroup> groups = new ArrayList<>();
//
//        DrawerGroup status = new DrawerGroup(res.getInteger(R.integer.drawer_status_id),
//                res.getString(R.string.drawer_status),
//                localPref.getBoolean(res.getString(R.string.drawer_status_is_expanded), true));
//        status.selectItem(localPref.getLong(res.getString(R.string.drawer_status_selected_item),
//                DrawerGroup.DEFAULT_SELECTED_ID));
//
//        DrawerGroup sorting = new DrawerGroup(res.getInteger(R.integer.drawer_sorting_id),
//                res.getString(R.string.drawer_sorting),
//                localPref.getBoolean(res.getString(R.string.drawer_sorting_is_expanded), false));
//        final long DEFAULT_SORTING_ITEM = 1;
//        sorting.selectItem(localPref.getLong(res.getString(R.string.drawer_sorting_selected_item),
//                DEFAULT_SORTING_ITEM));
//
//        DrawerGroup dateAdded = new DrawerGroup(res.getInteger(R.integer.drawer_date_added_id),
//                res.getString(R.string.drawer_date_added),
//                localPref.getBoolean(res.getString(R.string.drawer_time_is_expanded), false));
//        dateAdded.selectItem(localPref.getLong(res.getString(R.string.drawer_time_selected_item),
//                DrawerGroup.DEFAULT_SELECTED_ID));
//
//        status.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_status_all_id),
//                R.drawable.ic_all_inclusive_grey600_24dp, res.getString(R.string.all)));
//        status.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_status_downloading_id),
//                R.drawable.ic_download_grey600_24dp, res.getString(R.string.drawer_status_downloading)));
//        status.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_status_downloaded_id),
//                R.drawable.ic_file_grey600_24dp, res.getString(R.string.drawer_status_downloaded)));
//        status.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_status_downloading_metadata_id),
//                R.drawable.ic_magnet_grey600_24dp, res.getString(R.string.drawer_status_downloading_metadata)));
//        status.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_status_error),
//                R.drawable.ic_error_grey600_24dp, res.getString(R.string.drawer_status_error)));
//
//
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_date_added_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_date_added)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_date_added_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_date_added)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_name_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_name)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_name_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_name)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_size_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_size)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_size_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_size)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_progress_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_progress)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_progress_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_progress)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_ETA_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_ETA)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_ETA_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_ETA)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_peers_asc_id),
//                R.drawable.ic_sort_ascending_grey600_24dp, res.getString(R.string.drawer_sorting_peers)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_peers_desc_id),
//                R.drawable.ic_sort_descending_grey600_24dp, res.getString(R.string.drawer_sorting_peers)));
//        sorting.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_sorting_no_sorting_id),
//                R.drawable.ic_sort_off_grey600_24dp, res.getString(R.string.drawer_sorting_no_sorting)));
//
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_all_id),
//                R.drawable.ic_all_inclusive_grey600_24dp, res.getString(R.string.all)));
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_today_id),
//                R.drawable.ic_calendar_today_grey600_24dp, res.getString(R.string.drawer_date_added_today)));
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_yesterday_id),
//                R.drawable.ic_calendar_yesterday_grey600_24dp, res.getString(R.string.drawer_date_added_yesterday)));
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_week_id),
//                R.drawable.ic_calendar_week_grey600_24dp, res.getString(R.string.drawer_date_added_week)));
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_month_id),
//                R.drawable.ic_calendar_month_grey600_24dp, res.getString(R.string.drawer_date_added_month)));
//        dateAdded.items.add(new DrawerGroupItem(res.getInteger(R.integer.drawer_date_added_year_id),
//                R.drawable.ic_calendar_year_grey600_24dp, res.getString(R.string.drawer_date_added_year)));
//
//        groups.add(status);
//        groups.add(sorting);
//        groups.add(dateAdded);
//
//        return groups;
//    }

//    public static TorrentSortingComparator getDrawerGroupItemSorting(@NonNull Context context,
//                                                                     long itemId)
//    {
//        Resources res = context.getResources();
//        if (itemId == res.getInteger(R.integer.drawer_sorting_no_sorting_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.none, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_name_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.name, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_name_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.name, TorrentSorting.Direction.DESC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_size_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.size, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_size_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.size, TorrentSorting.Direction.DESC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_date_added_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.dateAdded, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_date_added_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.dateAdded, TorrentSorting.Direction.DESC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_progress_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.progress, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_progress_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.progress, TorrentSorting.Direction.DESC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_ETA_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.ETA, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_ETA_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.ETA, TorrentSorting.Direction.DESC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_peers_asc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.peers, TorrentSorting.Direction.ASC));
//        else if (itemId == res.getInteger(R.integer.drawer_sorting_peers_desc_id))
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.peers, TorrentSorting.Direction.DESC));
//        else
//            return new TorrentSortingComparator(new TorrentSorting(TorrentSorting.SortingColumns.none, TorrentSorting.Direction.ASC));
//    }
//
//    public static TorrentFilter getDrawerGroupStatusFilter(@NonNull Context context,
//                                                           long itemId)
//    {
//        Resources res = context.getResources();
//        if (itemId == res.getInteger(R.integer.drawer_status_all_id))
//            return TorrentFilterCollection.all();
//        else if (itemId == res.getInteger(R.integer.drawer_status_downloading_id))
//            return TorrentFilterCollection.statusDownloading();
//        else if (itemId == res.getInteger(R.integer.drawer_status_downloaded_id))
//            return TorrentFilterCollection.statusDownloaded();
//        else if (itemId == res.getInteger(R.integer.drawer_status_downloading_metadata_id))
//            return TorrentFilterCollection.statusDownloadingMetadata();
//        else if (itemId == res.getInteger(R.integer.drawer_status_error))
//            return TorrentFilterCollection.statusError();
//        else
//            return TorrentFilterCollection.all();
//    }
//
//    public static TorrentFilter getDrawerGroupDateAddedFilter(@NonNull Context context,
//                                                              long itemId)
//    {
//        Resources res = context.getResources();
//        if (itemId == res.getInteger(R.integer.drawer_date_added_all_id))
//            return TorrentFilterCollection.all();
//        else if (itemId == res.getInteger(R.integer.drawer_date_added_today_id))
//            return TorrentFilterCollection.dateAddedToday();
//        else if (itemId == res.getInteger(R.integer.drawer_date_added_yesterday_id))
//            return TorrentFilterCollection.dateAddedYesterday();
//        else if (itemId == res.getInteger(R.integer.drawer_date_added_week_id))
//            return TorrentFilterCollection.dateAddedWeek();
//        else if (itemId == res.getInteger(R.integer.drawer_date_added_month_id))
//            return TorrentFilterCollection.dateAddedMonth();
//        else if (itemId == res.getInteger(R.integer.drawer_date_added_year_id))
//            return TorrentFilterCollection.dateAddedYear();
//        else
//            return TorrentFilterCollection.all();
//    }

    /**
     * 根据groupName获取相应展示的颜色
     * @param firstLetters 对应社区首字母
     * @return 色值
     */
    public static int getGroupColor(String firstLetters) {
        Context context = MainApplication.getInstance();
        Resources res = context.getResources();
        int[] colors = res.getIntArray(R.array.group_color);
        int charCount = 0;
        if(StringUtil.isNotEmpty(firstLetters)){
            char[] chars = firstLetters.toCharArray();
            for (char aChar : chars) {
                charCount += aChar;
            }
        }
        return colors[charCount % colors.length];
    }

    /**
     * 验证URL是否合法
     * @param url URL
     * @return 是否合法
     */
    public static boolean validateUrl(String url) {
        String strRegex = "^"
                + "(([0-9]{1,3}.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})" // 端口- :80
                + "$";
        Pattern pattern = Pattern.compile(strRegex);
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }

    public static long getMedianData(List<Long> total) {
        if(total.size() > 0){
            int size = total.size();
            if(size % 2 == 1){
                return total.get((size -1 ) / 2);
            }else {
                return (long) ((total.get(size / 2 - 1) + total.get(size / 2) + 0.0) / 2);
            }
        }
        return 0;
    }
}