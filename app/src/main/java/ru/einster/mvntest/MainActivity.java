package ru.einster.mvntest;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private long downloadReference;


    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Обрабатываем скачивание файла приложения
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Убдимся что это наш файл
            if (downloadReference == referenceId) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //Создаем интен установки приложения
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(downloadManager.getUriForDownloadedFile(downloadReference),
                        "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(installIntent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateVersionLabel();

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private String getAppVersion() {
        String version = "0.0.0";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void updateVersionLabel() {
        String version = getAppVersion();
        TextView versionLabel = (TextView) findViewById(R.id.versionLabel);
        versionLabel.setText(version);
    }

    private void handleNewVersion(String version) {
        UpdateDialogFragment fragment = new UpdateDialogFragment();
        Bundle args = new Bundle();
        args.putString(UpdateDialogFragment.VERSION, version);
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), "updDlg");
    }

    public void checkForUpdate(View view) {
        new CheckUpdateAsyncTask().execute();
    }


    public void setDownloadReference(long downloadReference) {
        this.downloadReference = downloadReference;
    }


    private class CheckUpdateAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Repository repository = new Repository(getString(R.string.repository_url));
            List<String> versions = repository.getVersions(getString(R.string.artifact_group),
                    getString(R.string.artifact_id));
            String lastVersion = versions.get(versions.size() - 1);
            if (isVersionNewer(getAppVersion(), lastVersion)) {
                return lastVersion;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                handleNewVersion(s);
            } else {
                Toast toast = Toast.makeText(MainActivity.this, getString(R.string.no_updates), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        /*
         Метод сравнения версий вида x.x.x
         */
        private boolean isVersionNewer(String current, String mvnVersion) {
            String[] currentParts = current.split("\\.");
            String[] mvnParts = mvnVersion.split("\\.");
            for (int i = 0; i < 3; i++) {
                if (compare(currentParts[i], mvnParts[i]) < 0) {
                    return true;
                }
            }
            return false;
        }

        private int compare(String v1, String v2) {
            Integer v1Int = Integer.parseInt(v1);
            Integer v2Int = Integer.parseInt(v2);
            return v1Int.compareTo(v2Int);
        }
    }


}
