package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Send {

    private static final int TIMEOUT = 25000;

    private static final String SERVER_IP = "http://192.168.43.54:3000/";

    public static MediaPath sendVideo(MediaPath mediaPath){
        try {
            URL url = new URL(SERVER_IP + "upload-video");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");

            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);

            OutputStream os = conn.getOutputStream();
            os.write(mediaPath.getData());

            os.flush();
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            Bitmap b = BitmapFactory.decodeStream(in);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss_SSS").format(new Date());
            String name = "IMG_" + timeStamp + ".png";
            File file = new File(mediaPath.getPath() + File.separator + "IMG_" + timeStamp + ".png");

            OutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(byteArray);

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            conn.disconnect();

            return new MediaPath(file.getName() ,b, name);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getReceiptData(Bitmap image) {
        try
        {
            URL url = new URL(SERVER_IP + "scan");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            //conn.setFixedLengthStreamingMode(1024);

            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);

            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 30, blob);
            byte[] data = blob.toByteArray();

            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            System.out.println("Response Code: " + conn.getResponseCode());

            InputStream in = new BufferedInputStream(conn.getInputStream());

            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");

            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println(response);

            conn.disconnect();
            return response;
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
