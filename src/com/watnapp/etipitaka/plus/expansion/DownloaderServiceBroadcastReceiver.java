package com.watnapp.etipitaka.plus.expansion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.watnapp.etipitaka.plus.expansion.DownloaderService;

/**
 * Created by sutee on 5/6/19.
 */
public class DownloaderServiceBroadcastReceiver extends android.content.BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, DownloaderService.class);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }
}
