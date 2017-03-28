package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Send {

    private static final String SERVER_IP = "http://192.168.43.54:3000/";

    public static void sendVideo(byte[] bytes){
        try {

            Log.d("Camera2Basic", "in send video");
            //Thread.sleep(2000);
            URL url = new URL(SERVER_IP + "upload-video");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");

            conn.setReadTimeout(35000);
            conn.setConnectTimeout(35000);

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            //osw.write(message);
            os.write(bytes);
            osw.flush();
            osw.close();

            os.flush();
            os.close();

            System.out.println("Response Code: " + conn.getResponseCode());

            InputStream in = new BufferedInputStream(conn.getInputStream());
            Log.d("sdfs", "sfsd");
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");
            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println(response);

            conn.disconnect();
            Log.d("Camera2Basic", "in send video done");

            //CameraActivity.sentToList.add(receiver);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    public static void sendImage(Bitmap source) {
        try {
            //Thread.sleep(2000);
            URL url = new URL(SERVER_IP + "upload-video");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");

            conn.setReadTimeout(35000);
            conn.setConnectTimeout(35000);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            int compress = 75;

            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            source.compress(Bitmap.CompressFormat.JPEG, 30, blob);
            byte[] data = blob.toByteArray();

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            //osw.write(message);
            os.write(data);
            osw.flush();
            osw.close();

            os.flush();
            os.close();

            System.out.println("Response Code: " + conn.getResponseCode());

            InputStream in = new BufferedInputStream(conn.getInputStream());
            Log.d("sdfs", "sfsd");
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");
            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println(response);

            conn.disconnect();
            ContactFragment.iconMap.put(receiver, R.mipmap.sent);
            //CameraActivity.sentToList.add(receiver);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
