package com.siahmsoft.soundwaper;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/*
* Copyright (C) 2010 Siahmsoft
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
public final class BitmapHelper {

  public static Bitmap scale(Bitmap b, float scale) {
    Matrix matrix = new Matrix();
    matrix.postScale(scale, scale);
    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    
    return b;
  }
}
