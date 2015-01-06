package com.squareup.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;

class ByteStreamBitmapHunter extends BitmapHunter {
  protected static final String SCHEME_BYTE = "byte";
  private static final int BYTE_PREFIX_LENGTH =
      (SCHEME_BYTE + "://").length();

  public ByteStreamBitmapHunter(Context context, Picasso picasso, Dispatcher dispatcher, Cache cache,
                                Stats stats, Action action) {
    super(picasso, dispatcher, cache, stats, action);
  }

  @Override Bitmap decode(Request data) throws IOException {
    String filePath = data.uri.toString().substring(BYTE_PREFIX_LENGTH);
    Bitmap bitmap = null;
    try {
        bitmap = decodeAsset(filePath);
    } catch (Exception e) {
        e.printStackTrace();
    }

    return bitmap;
  }

  @Override Picasso.LoadedFrom getLoadedFrom() {
    return DISK;
  }

  Bitmap decodeAsset(String filePath) throws IOException {
    final BitmapFactory.Options options = createBitmapOptions(data);
    byte[] imageData = null;
    if (requiresInSampleSize(options)) {
        calculateInSampleSize(data.targetWidth, data.targetHeight, options);
    }

    try {
        MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
        mmdr.setDataSource(filePath);
        imageData = mmdr.getEmbeddedPicture();
    } catch (Exception e) {
        return null;
    }

    ByteArrayInputStream is = new ByteArrayInputStream(imageData);
    try {
      return BitmapFactory.decodeStream(is, null, options);
    } finally {
      Utils.closeQuietly(is);
    }
  }
}
