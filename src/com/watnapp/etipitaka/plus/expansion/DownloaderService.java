package com.watnapp.etipitaka.plus.expansion;

/**
 * Created by sutee on 5/6/19.
 */
public class DownloaderService extends com.google.android.vending.expansion.downloader.impl.DownloaderService {
  public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAizCAc3phNuyay1pL9LLLZdzgkvmzkBC3mAIajnD19VufNfH2aaGCS9tOGc1IGHImxLVfrIoj9guc+Ewpyj9HlQkTn5725C2rfUayTMrvhmiwxN9RF1MS+ixQ6DckMJQPGC2UExxT+v3syILfxZMj3zpjVF9zJdUgQC+GTvMT7QNv/xOoJFzo5GcvzLLnQRFAk5A+rnPmSHI83DGJTOBX48DsTB1zt93jMXezMDUjaWbeDoFFqi/VZ4sTfqPGwSsq3wpa3e0W2o6DZlhlyOqewxHokSLgsdu4C77WWM3P3wO0+ZpEHadwcVIZaA6279KtTnwGlJWg+4hejap28/IAyQIDAQAB";
  private static final byte[] SALT = new byte[]{9, 0, -1, -9, 14, 42, -79, -21, 13, 2, -3, -11, 72, 1, -10, -101, -19, 49, -12, 8}; // TODO Replace with random numbers of your choice. (it is just to ensure that the expansion files are encrypted with a unique key from your app)

  @Override
  public String getPublicKey() {
    return BASE64_PUBLIC_KEY;
  }

  @Override
  public byte[] getSALT() {
    return SALT;
  }

  @Override
  public String getAlarmReceiverClassName() {
    return DownloaderServiceBroadcastReceiver.class.getName();
  }
}