package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Send {

    private static final int TIMEOUT = 65000;
    private static final int HDR_TIMEOUT = 650000;
    private static final String SERVER_IP = "http://35.187.9.169:80/";

    /**
     * Sends captured video data to the server.
     *
     * @param mediaPath contains video data to send
     * @return Return a media path that contains image and a path.
     */
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

            String message = "start" + MainActivity.token;

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

            os.write(mediaPath.getData());
            osw.write(message);

            osw.flush();
            osw.close();

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

    /**
     * Sends the image to the server to OCR-scan it with Google Cloud Vision.
     *
     * @param image image to OCR scan
     * @return The text that the OCR scan read.
     */
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

    /**
     * Sends three images with different exposure time to be processed by HDR algorithms on the server.
     *
     * @param dir The path where the images will be written
     * @param path Image name
     * @param data The images in raw format
     * @param algorithms the name of the algorithms we want the server to process
     * @return A list containing the names of all the files.
     */
    public static List<String> sendImage(File dir, String path, List<Byte[]> data, List<String> algorithms) {

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

                conn.setReadTimeout(HDR_TIMEOUT);
                conn.setConnectTimeout(HDR_TIMEOUT);

                String message = "start" + path + "," + i;

                Byte[] buffer = data.get(i);

                byte[] bytes = toPrimitives(buffer);

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

        List<String> names = new ArrayList<>();
        for(String s : algorithms) {
            String fileName = getHDR(dir, path, s);
            names.add(fileName);
        }

        return names;
    }

    private static String getHDR(File dir, String path, String method){
        try {
            URL url = new URL(SERVER_IP + "get-hdr");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "multipart/form-data");

            conn.setReadTimeout(HDR_TIMEOUT);
            conn.setConnectTimeout(HDR_TIMEOUT);

            String message = path + "," + method + "," + MainActivity.token;

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
            String name = "IMG_" + timeStamp + "_" + method + ".jpg";
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

    /**
     * Writes the file the internal storage on the device
     *
     * @param file the file containing the internal path.
     * @param data The image in raw format
     */
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

    /**
     * Converts object bytes to primitive Bytes
     *
     * @param oBytes The data source in object form
     * @return The byte array in primitive form
     */
    public static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for(int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }

    /**
     * Converts primitive Bytes to object bytes
     *
     * @param bytesPrim the bytes in object form
     * @return The byte array in primitive form
     */
    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];

        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; // Autoboxing

        return bytes;
    }
}