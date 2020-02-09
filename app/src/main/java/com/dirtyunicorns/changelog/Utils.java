/*
 * Copyright (C) 2020 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dirtyunicorns.changelog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Utils {

    public static String getProp(String prop, Boolean substring) {
        Process process;
        try {
            process = new ProcessBuilder("/system/bin/getprop", prop).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                prop = substring ? line.substring(0, 16) : line.
                        replace("UNOFFICIAL-", "").
                        replace("RC-", "").
                        replace("OFFICIAL-", "").
                        replace("WEEKLIES-", "");
            }
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return Objects.requireNonNull(wifi).isConnected()
                || Objects.requireNonNull(mobile).isConnected();
    }
}