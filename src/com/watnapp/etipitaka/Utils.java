package com.watnapp.etipitaka;

import android.content.Context;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.helper.BookDatabaseHelper.Language;
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
    return context.getString(R.string.subtitle_template,
        context.getString(language == Language.THAI
            ? R.string.thai_full_name : R.string.pali_full_name),
        Utils.convertToThaiNumber(context, volume),
        Utils.convertToThaiNumber(context, page), item);
  }
}
