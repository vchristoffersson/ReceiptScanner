package kandidat30.receiptscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v13.app.FragmentCompat;



public class CameraFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback {

    private static final String APP_DIRECTORY = "ReceiptScanner";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private static final int REQUEST_MULTIPLE_PERMISSION = 300;
    private static final int AMOUNT_OF_PICTURES = 3;
    private static final long[] EXPOSURE_TIMES = new long[]{};
    private AutoFitTextureView cameraView;
    private Button cameraButton;
    private CameraDevice device;
    private CameraManager manager;
    private CameraCharacteristics characteristics;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imgSize;
    private File file;
    private Image image;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private ImageReader imageReader;
    private Range<Long> exposureRange;
    private Range<Integer> exposureCompensationRange;
    private List<CaptureRequest> burst;
    private File directory;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        cameraButton = (Button) view.findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureFromCamera();
            }
        });
        cameraView = (AutoFitTextureView) view.findViewById(R.id.camera_view);
        cameraView.setSurfaceTextureListener(surfaceTextureListener);

        return view;
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            initCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void initCamera() {
        if(!createDir()){
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            Log.d("STORAGE", "failed to create directory, using default");
        }

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String id = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            StreamConfigurationMap configurationMap = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configurationMap != null) {
                imgSize = configurationMap.getOutputSizes(SurfaceTexture.class)[0];
            }
            //Check for hardware level to see if exposure settings are available
            int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (deviceLevel) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d("EXPOSURE", "level is full");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d("EXPOSURE", "level is legacy");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d("EXPOSURE", "level is limited");
                    break;
            }
            exposureCompensationRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            if(exposureCompensationRange != null) {
                Log.d("EXPOSURE", exposureCompensationRange.toString());
            } else {
                Log.d("EXPOSURE", "exposure was null, check permissions or hardware level");
            }


            //Check for available exposure value range to set it correctly
            exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            if(exposureRange != null) {
                Log.d("EXPOSURE", exposureRange.toString());
            } else {
                Log.d("EXPOSURE", "exposure was null, check permissions or hardware level");
            }
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
               // FragmentCompat.requestPermissions(, new String[]{Manifest.permission.CAMERA,
                 //       Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MULTIPLE_PERMISSION);
            }

            manager.openCamera(id, callback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean createDir() {
        directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + APP_DIRECTORY);
        if(!directory.exists() && !directory.isDirectory()) {
            return directory.mkdirs();
        } else {
            //already exists and is a directory
            return true;
        }

    }

    private void captureFromCamera() {
        Log.d("STORAGE", "init capturefromcamera");

        if(device == null){
            return;
        }

        manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            characteristics = manager.getCameraCharacteristics(device.getId());

            //@TODO look into saving a picture in the RAW format instead, which is not processed as a JPEG is
            Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);

            int width = sizes[0].getWidth();
            int height = sizes[0].getHeight();

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(reader.getSurface());
            surfaces.add(new Surface(cameraView.getSurfaceTexture()));

            final CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(reader.getSurface());
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            builder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            //@TODO create multiple requests to add to List<CaptureRequest> and later add them to a captureBurst()
            //@TODO set the exposures for the current photos through exposureRange
            /*
            burst = new ArrayList<>();
            if (exposureRange != null) {
                for (int i = 0; i < AMOUNT_OF_PICTURES; i++) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime);
                    //It's possible not to set these and then you get a default value, depending on
                    //the picture this can be or not be good enough..
                    builder.set(CaptureRequest.SENSOR_SENSITIVITY, sensitivity);
                    builder.set(CaptureRequest.SENSOR_FRAME_DURATION, frameDuration);
                    burst.add(builder.build());
                }
            }
            */
            burst = new ArrayList<>();
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -2);
            burst.add(builder.build());
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            burst.add(builder.build());
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 2);
            burst.add(builder.build());


            Log.d("STORAGE", "Store in: " + directory.getPath());
            ImageReader.OnImageAvailableListener imgReaderListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss_SSS").format(new Date());

                    file = new File(directory + File.separator + "IMG_" + timeStamp + ".jpg");
                    backgroundHandler.post(new ImageSaver(file, reader.acquireNextImage()));
/*                    image = reader.acquireLatestImage();
                    if(image == null){
                        Log.d("CAMERA", "image was null");
                    } else {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        storeBytes(bytes);
                    }*/
                }

                private void storeBytes(byte[] bytes) {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(imgReaderListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback sessionListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request
                        , @NonNull TotalCaptureResult result){
                    super.onCaptureCompleted(session, request, result);

                    try {
                        Log.d("CAMERA", Long.toString(result.get(TotalCaptureResult.SENSOR_EXPOSURE_TIME)));
                    } catch (NullPointerException e){
                        e.printStackTrace();
                        Log.d("CAMERA", "exposure time not found");
                    }
                    initPreview();
                }
            };

            device.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        //@TODO add several requests to captureBurst()
                        //session.capture(builder.build(), sessionListener, backgroundHandler);
                        Log.d("BURST", "before burst" + burst.size());
                        //session.stopRepeating();
                        session.setRepeatingBurst(burst, sessionListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initPreview() {
        SurfaceTexture surfaceTexture = cameraView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(imgSize.getWidth(), imgSize.getHeight());
        final Surface surface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            device.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (device != null) {
                        captureSession = session;
                        //This requires android api 23
                        try {
                            captureSession.prepare(surface);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        updatePreview();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(device != null){
            device.close();
            device = null;
        }
        if(imageReader != null){
            imageReader.close();
            imageReader = null;
        }
    }

    private void startThread(){
        backgroundThread = new HandlerThread("background thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopThread(){
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback callback = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            device = camera;
            initPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            device.close();
        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            device.close();
            device = null;
        }
    };

    private CameraCaptureSession.CaptureCallback callbackListener = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession captureSession, @NonNull
            CaptureRequest captureRequest, @NonNull TotalCaptureResult captureResult) {

            super.onCaptureCompleted(captureSession, captureRequest, captureResult);
            initPreview();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.d("PERMISSION", "checkin if permission was granted");
        switch (requestCode) {
            case REQUEST_MULTIPLE_PERMISSION: {
                Map<String, Integer> permissionsMap = new HashMap<>();
                permissionsMap.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                permissionsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                for (int i = 0; i < permissions.length; i++)
                    permissionsMap.put(permissions[i], grantResults[i]);

                if (permissionsMap.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && permissionsMap.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Log.d("PERMISSION", "All permissions was granted");
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                } else {
                    Log.d("PERMISSION", "All permissions was not granted");
                }
                break;
            }
            case REQUEST_CAMERA_PERMISSION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("PERMISSION", "Camera permission was granted");
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                } else {
                    Log.d("PERMISSION", "Camera permission was denied");

                }
                break;
            }
            case REQUEST_STORAGE_PERMISSION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("PERMISSION", "Write_storage permission was granted");
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                } else {
                    Log.d("PERMISSION", "Write_storage permission was denied");
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startThread();
        if(cameraView.isAvailable()){
            initCamera();
        } else {
            cameraView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause(){
        stopThread();
        super.onPause();
    }
}
