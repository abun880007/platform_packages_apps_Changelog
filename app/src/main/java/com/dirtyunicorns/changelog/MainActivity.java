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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.dirtyunicorns.changelog.Utils.getProp;
import static com.dirtyunicorns.changelog.Utils.isConnected;

public class MainActivity extends Activity {

    private Context mContext;

    private boolean changelogAvailable = false;

    private String changelog;
    private String urlChangelog;
    private String urlGithub;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        urlChangelog = mContext.getString(R.string.changelog_url);
        urlGithub = mContext.getString(R.string.dialog_github_url);

        if (isConnected(mContext)) {
            new getChangelogData().execute();
        } else {
            showChangelogDialog();
        }
    }

    class getChangelogData extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection = null;

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            URL url;
            String data;
            try {
                if (getProp("ro.mod.version", true).startsWith("WEEKLIES")) {
                    url = new URL(urlChangelog + "weeklies" + "/" +
                            getProp("ro.mod.version", false));
                    changelogAvailable = true;
                } else if (getProp("ro.mod.version", true).startsWith("OFFICIAL")) {
                    url = new URL(urlChangelog + "official" + "/" +
                            getProp("ro.mod.version", false));
                    changelogAvailable = true;
                } else if (getProp("ro.mod.version", true).startsWith("RC")) {
                    url = new URL(urlChangelog + "rc" + "/" +
                            getProp("ro.product.device", false) + "/" +
                            getProp("ro.mod.version", false));
                    changelogAvailable = true;
                } else {
                    url = new URL(urlChangelog + "not-found");
                    changelogAvailable = false;
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(url.openStream()));

                while ((data = br.readLine()) != null) {
                    result.append(data).append("\n");
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    try {
                        url = new URL(urlChangelog + "not-found");
                        changelogAvailable = false;

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(true);
                        urlConnection.connect();

                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(url.openStream()));

                        while ((data = br.readLine()) != null) {
                            result.append(data).append("\n");
                        }
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                changelog = jsonObject.getString("changelog").replaceAll("'","\'");

                showChangelogDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void showChangelogDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.changelog_dialog, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        TextView dialogTitle = dialogView.findViewById(R.id.title);
        TextView dialogChangelog = dialogView.findViewById(R.id.summary);

        if (changelogAvailable) {
            dialogTitle.setText(String.format("%s - %s",
                    mContext.getString(R.string.changelog_title),
                    getProp("ro.build.date", true)));
            dialogChangelog.setText(changelog);
        } else if (!isConnected(mContext)) {
            dialogTitle.setText(mContext.getString(R.string.changelog_no_connection_title));
            dialogChangelog.setText(mContext.getString(R.string.changelog_no_connection_summary));
        } else {
            dialogTitle.setText(mContext.getString(R.string.changelog_title));
            dialogChangelog.setText(mContext.getString(R.string.changelog_unavailable));
        }

        dialog.setPositiveButton(mContext.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });
        if (isConnected(mContext) && changelogAvailable) {
            dialog.setNegativeButton(mContext.getString(R.string.dialog_github),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                finish();
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(urlGithub));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

        AlertDialog builder = dialog.create();

        builder.show();
    }
}