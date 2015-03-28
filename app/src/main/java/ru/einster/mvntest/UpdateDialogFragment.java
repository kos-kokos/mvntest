package ru.einster.mvntest;

import android.app.AlertDialog;
import android.app.Dialog;

import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;


/**
 * DialogFragment с диалогом установки обновления
 */
public class UpdateDialogFragment extends DialogFragment {

    public static final String VERSION = "version";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String msg = getString(R.string.install_version, getArguments().getString(VERSION));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startDownloadingApk(getArguments().getString(VERSION));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    /**
     * Запуск загрузки новой версии приложения
     *
     * @param version
     */
    private void startDownloadingApk(String version) {
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        Uri Download_Uri = Uri.parse(Repository.getArtifactUrl(
                getString(R.string.repository_url),
                getString(R.string.artifact_group),
                getString(R.string.artifact_id),
                version));

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        request.setTitle(getString(R.string.download_title));
        request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, "mvntest.apk");
        long downloadReference = downloadManager.enqueue(request);
        MainActivity activity = (MainActivity) getActivity();
        activity.setDownloadReference(downloadReference);
    }

}
