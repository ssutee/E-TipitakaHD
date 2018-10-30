package com.watnapp.etipitaka.plus;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/5/2013
 * Time: 20:19
 */
public class Utils {
  public static String convertToThaiNumber(Context context, int number) {
    StringBuilder sb = new StringBuilder();
    String[] thaiNumbers = context.getResources().getStringArray(R.array.thai_numbers);
    for (char c : String.valueOf(number).toCharArray()) {
      try {
        sb.append(thaiNumbers[Integer.parseInt(String.valueOf(c))]);
      } catch (NumberFormatException e) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static String convertToArabicNumber(Context context, String number) {
    String[] thaiNumbers = context.getResources().getStringArray(R.array.thai_numbers);
    String result = number;
    for (int i=0; i<10; ++i) {
      result = result.replaceAll(thaiNumbers[i], String.valueOf(i));
    }
    return result;
  }

  public static String getSubtitle(Context context, Language language, int volume, int page, String item) {
    return item.length() == 0
        ? context.getString(R.string.subtitle_noitem_template, language.getFullName(context), Utils.convertToThaiNumber(context, volume), Utils.convertToThaiNumber(context, page))
        : context.getString(R.string.subtitle_template, language.getFullName(context), Utils.convertToThaiNumber(context, volume), Utils.convertToThaiNumber(context, page), item);
  }

  public static String readTextFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
    StringBuffer sb = new StringBuffer();
    String line;
    while((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }

  public static String getMD5Checksum(String path) throws IOException {
    HashCode md5 = Files.hash(new File(path), Hashing.md5());
    return md5.toString();
  }

  public static boolean isTipitaka(Language language) {
    return language != Language.THAIBT &&
        language != Language.THAIWN &&
        language != Language.THAIVN &&
        language != Language.THAIPB;
  }

  public static String getDatabasePath(Language language) {
    String databaseDir = Utils.getDatabaseDirectory();
    switch (language) {
      case THAIMC:
        return databaseDir + "/" + "thaimc.db";
      case THAIMM:
        return databaseDir + "/" + "thaimm.db";
      case THAIBT:
        return databaseDir + "/" + "thaibt.db";
      case THAIPB:
        return databaseDir + "/" + "thaipb.db";
      case THAIWN:
        return databaseDir + "/" + "thaiwn.db";
      case ROMANCT:
        return databaseDir + "/" + "romanct.db";
      case THAI:
        return databaseDir + "/" + "thai.db";
      case PALI:
        return databaseDir + "/" + "pali.db";
      case THAIVN:
        return databaseDir + "/" + "thaivn.db";
      default:
        return databaseDir + "/" + "thai.db";
    }
  }

  public static String getDatabaseDirectory() {
    return E_TipitakaApplication.getAppContext().getExternalFilesDir(null).getPath();
  }

  public static String floatForm (double d)
  {
    return new DecimalFormat("#.##").format(d);
  }


  public static String bytesToHuman (long size)
  {
    long Kb = 1  * 1024;
    long Mb = Kb * 1024;
    long Gb = Mb * 1024;
    long Tb = Gb * 1024;
    long Pb = Tb * 1024;
    long Eb = Pb * 1024;

    if (size <  Kb)                 return floatForm(        size     ) + " byte";
    if (size >= Kb && size < Mb)    return floatForm((double)size / Kb) + " Kb";
    if (size >= Mb && size < Gb)    return floatForm((double)size / Mb) + " Mb";
    if (size >= Gb && size < Tb)    return floatForm((double)size / Gb) + " Gb";
    if (size >= Tb && size < Pb)    return floatForm((double)size / Tb) + " Tb";
    if (size >= Pb && size < Eb)    return floatForm((double)size / Pb) + " Pb";
    if (size >= Eb)                 return floatForm((double)size / Eb) + " Eb";

    return "???";
  }

}
