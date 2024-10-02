package com.example.chat_east;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class image_util_profile {

    public static File SaveImage(Context context, String imageUrl, String fileName) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return null;
        }

        File file = null;
        try {
            File folder = new File(context.getCacheDir(), "C_E_preview");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input_stream = connection.getInputStream();

            File temp_file = new File(folder, "temp_image.jpg");
            FileOutputStream temp_output_stream = new FileOutputStream(temp_file);
            byte[] buffer = new byte[1024];
            int bytes_read;
            while ((bytes_read = input_stream.read(buffer)) != -1) {
                temp_output_stream.write(buffer, 0, bytes_read);
            }
            temp_output_stream.close();
            input_stream.close();

            ExifInterface exif = new ExifInterface(temp_file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Bitmap bitmap = BitmapFactory.decodeFile(temp_file.getAbsolutePath());

            Bitmap rotated_bitmap = RotateBitMap(bitmap, orientation);

            file = new File(folder, fileName);
            FileOutputStream output_stream = new FileOutputStream(file);
            rotated_bitmap.compress(Bitmap.CompressFormat.JPEG, 30, output_stream); // Increased quality
            output_stream.flush();
            output_stream.close();

            temp_file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    private static Bitmap RotateBitMap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
