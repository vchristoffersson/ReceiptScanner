package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Send {

    private static final int TIMEOUT = 25000;
    private static final String SERVER_IP = "http://192.168.1.7:3000/";

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
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss_SSS").format(new Date());
            String name = "IMG_" + timeStamp + ".jpg";
            File file = new File(mediaPath.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

            writeFile(file, byteArray);

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

            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);

            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 30, blob);
            byte[] data = blob.toByteArray();

            OutputStream os = conn.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));

            String line;
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

    public static String sendImage(File dir, String path, List<Bitmap> data) {

        for(int i = 0; i < data.size(); i++) {

            try {
                URL url = new URL(SERVER_IP + "upload-image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "multipart/form-data");

                conn.setReadTimeout(TIMEOUT);
                conn.setConnectTimeout(TIMEOUT);

                String message = "start" + path + "," + i;

                Bitmap b = data.get(i);
                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 30, blob);
                byte[] bytes = blob.toByteArray();

                OutputStream os = conn.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(message);
                os.write(bytes);
                osw.flush();
                osw.close();

                os.flush();
                os.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());

                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null)
                    stringBuilder.append(line).append("\n");

                responseStreamReader.close();

                String response = stringBuilder.toString();
                System.out.println(response);

                conn.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return getHDR(dir, path);
    }

    private static String getHDR(File dir, String path){
        try {
            URL url = new URL(SERVER_IP + "get-hdr");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");

            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);

            String method = "mertens";
            String message = path + "," + method;

            DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
            ds.writeBytes(message);
            ds.flush();
            ds.close();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            Bitmap b = BitmapFactory.decodeStream(in);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss_SSS").format(new Date());
            String name = "IMG_" + timeStamp + ".jpg";
            File file = new File(dir + File.separator + name);

            writeFile(file, byteArray);

            conn.disconnect();

            return name;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void writeFile(File file, byte[] data) {

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(data);

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
    }

    private static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }
}
