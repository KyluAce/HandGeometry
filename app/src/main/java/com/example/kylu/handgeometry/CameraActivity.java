package com.example.kylu.handgeometry;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CameraActivity extends Activity implements CvCameraViewListener2 {

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("openCv", "OpenCV loaded sucessfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };
    private CameraContour mOpenCvCameraView;
    private ImageButton imageButton;
    private LinearLayout linearLayout;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_camera);

        imageButton = (ImageButton) findViewById(R.id.imageButton);

        imageButton.setBackgroundResource(R.drawable.flashbutton);
        linearLayout = (LinearLayout) findViewById(R.id.imageLayout);

        mOpenCvCameraView = (CameraContour) findViewById(R.id.MainActivityCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void cameraActiv(View v) {

        if (mOpenCvCameraView.isLighOn()) {
            mOpenCvCameraView.setEffect(Parameters.FLASH_MODE_OFF);
            linearLayout.setBackgroundColor(Color.BLACK);
            imageButton.setBackgroundColor(Color.BLACK);
            imageButton.setBackgroundResource(R.drawable.flashbutton);
            //mOpenCvCameraView.setHightQuality();
        } else {
            mOpenCvCameraView.setEffect(Parameters.FLASH_MODE_TORCH);
            linearLayout.setBackgroundColor(Color.WHITE);
            imageButton.setBackgroundColor(Color.WHITE);
            imageButton.setBackgroundResource(R.drawable.flashwhite);
            //mOpenCvCameraView.setHightQuality();
        }

    }

    public void cameraClick(View v) {
        Intent intent = getIntent();

        user = new User(intent.getStringExtra("name"), intent.getStringExtra("surname").toString(), intent.getStringExtra("pesel"));

        new HttpCamera(this).execute("http://192.168.43.77:8080/geometry-1.0.0-BUILD-SNAPSHOT/add", toJson());
    }


    private String toJson() {

        JSONObject finalObject = new JSONObject();

        try {

            JSONArray jsonArray = new JSONArray();

            for (Point p : contour) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("x", p.x);
                jsonObject.put("y", p.y);
                jsonArray.put(jsonObject);
            }


            finalObject.put("PList", jsonArray);

        } catch (JSONException e) {

        }

        return finalObject.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private Mat mRgba;
    private Mat mIntermediateMat;
    private List<Point> contour;

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.gray();

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.GaussianBlur(mRgba, mRgba, new Size(5, 5), 0);

        Imgproc.threshold(mRgba, mIntermediateMat, 70, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {

                double a = Imgproc.contourArea(o1);
                double b = Imgproc.contourArea(o2);

                return a > b ? -1 : (a < b ? 1 : 0);
            }
        });

        Imgproc.drawContours(mRgba, contours, 0, new Scalar(0, 255, 255), 5);

        contour = contours.get(0).toList();

        return mRgba;
    }

    private ProgressDialog progressDialog;

    private class HttpCamera extends AsyncTask<String, Void, String> {

        private Activity activity;

        public HttpCamera(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpRequest httpRequest = new HttpRequest();
            Map<String, String> params = new HashMap<>();
            params.put("json", urls[1]);
            params.put("name", user.getName());
            params.put("surname", user.getSurname());
            params.put("pesel", user.getPesel());
            httpRequest.setParams(params);
            return httpRequest.get(urls[0]);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Obliczanie");
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            Toast.makeText(activity, "Uzytkownik i geometria pomyslnie dodana!!",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        }

    }
}
