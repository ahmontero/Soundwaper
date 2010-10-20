package com.siahmsoft.soundwaper;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 
 * Helper class to use with images
 *
 */
public final class BitmapHelper {

  public static Bitmap scale(Bitmap b, float scale) {
    Matrix matrix = new Matrix();
    matrix.postScale(scale, scale);
    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    
    return b;
  }
}
