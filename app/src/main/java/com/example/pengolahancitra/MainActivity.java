package com.example.pengolahancitra;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView, showGraph;
    Uri imageUri;
    Bitmap grayBitmap, imageBitmap;
    private boolean isImageHistogram = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //1
        if(OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"Open Cv Berhasil",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"gagal gan coba lagi",Toast.LENGTH_SHORT).show();
        }

        //2
        imageView = findViewById(R.id.imageView2);
        OpenCVLoader.initDebug();

        showGraph = findViewById(R.id.show_graph);

    }

    //3
    public void openGaleri(View view){
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myIntent,100);

    }

    public void histogram(){
        if(imageBitmap == null){
            Toast.makeText(this, "You must upload image from gallery to show its histogram", Toast.LENGTH_SHORT).show();
        }else{
            if(!isImageHistogram){
                //add histogram code
                Mat sourceMat = new Mat();
                Utils.bitmapToMat(imageBitmap, sourceMat);

                Size sourceSize = sourceMat.size();

                int histogramSize = 256;
                MatOfInt hisSize = new MatOfInt(histogramSize);

                Mat destinationMat = new Mat();
                List<Mat> channels = new ArrayList<>();

                MatOfFloat range = new MatOfFloat(0f, 255f);
                MatOfFloat histRange = new MatOfFloat(range);

                Core.split(sourceMat, channels);

                MatOfInt[] allChannel = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
                Scalar[] colorScalar = new Scalar[]{new Scalar(220, 0, 0, 255), new Scalar(0, 220, 0, 255), new Scalar(0, 0, 220, 255)};

                Mat matB = new Mat(sourceSize, sourceMat.type());
                Mat matG = new Mat(sourceSize, sourceMat.type());
                Mat matR = new Mat(sourceSize, sourceMat.type());

                Imgproc.calcHist(channels, allChannel[0], new Mat(), matB, hisSize, histRange);
                Imgproc.calcHist(channels, allChannel[1], new Mat(), matG, hisSize, histRange);
                Imgproc.calcHist(channels, allChannel[2], new Mat(), matR, hisSize, histRange);


                int graphHeight = 300;
                int graphWidth = 200;
                int binWidth = 3;

                Mat graphMat = new Mat(graphHeight, graphWidth, CvType.CV_8UC3, new Scalar(0, 0, 0));

                //Normalize channel
                Core.normalize(matB, matB, graphMat.height(), 0, Core.NORM_INF);
                Core.normalize(matG, matG, graphMat.height(), 0, Core.NORM_INF);
                Core.normalize(matR, matR, graphMat.height(), 0, Core.NORM_INF);

                //convert pixel value to point and draw line with points
                for(int i = 0; i < histogramSize; i++){
                    Point bPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matB.get(i - 1, 0)[0]));
                    Point bPoint2 = new Point(binWidth * i, graphHeight - Math.round(matB.get(i, 0)[0]));
                    Core.line(graphMat, bPoint1, bPoint2, new Scalar(220, 0, 0, 255), 3, 8, 0);

                    Point gPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matG.get(i - 1, 0)[0]));
                    Point gPoint2 = new Point(binWidth * i, graphHeight - Math.round(matG.get(i, 0)[0]));
                    Core.line(graphMat, gPoint1, gPoint2, new Scalar(0, 220, 0, 255), 3, 8, 0);

                    Point rPoint1 = new Point(binWidth * (i - 1), graphHeight - Math.round(matR.get(i - 1, 0)[0]));
                    Point rPoint2 = new Point(binWidth * i, graphHeight - Math.round(matR.get(i, 0)[0]));
                    Core.line(graphMat, rPoint1, rPoint2, new Scalar(0, 0, 220, 255), 3, 8, 0);
                }

                //convert Mat to bitmap
                Bitmap graphBitmap = Bitmap.createBitmap(graphMat.cols(), graphMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(graphMat, graphBitmap);

                // show histogram
                showGraph.setImageBitmap(graphBitmap);
                //set the isImageHistogram
                isImageHistogram = false;

            }
        }
    }

    //4
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null){

            imageUri = data.getData();

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            }catch (IOException e){
                e.printStackTrace();
            }

            imageView.setImageBitmap(imageBitmap);
            histogram();
        }
    }

    //6

    //5
    public void grayscale(View view){

        Bitmap bantu = imageBitmap;

        if(bantu == null){
            Toast.makeText(getApplicationContext(),"Masukkan Gambar Terlebih Dahulu",Toast.LENGTH_SHORT).show();
        }else {
            Mat Rgba = new Mat();
            Mat grayMat = new Mat();

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inDither = false;
            o.inSampleSize = 4;

            int widh = bantu.getWidth();
            int height = bantu.getHeight();

            grayBitmap = Bitmap.createBitmap(widh,height,Bitmap.Config.RGB_565);

            Utils.bitmapToMat(bantu,Rgba);
            Imgproc.cvtColor(Rgba,grayMat,Imgproc.COLOR_RGB2GRAY);

            Utils.matToBitmap(grayMat,grayBitmap);

            imageView.setImageBitmap(grayBitmap);

            histogram();
        }
    }

    public void DeteksiTepiKeny(View view){

        Bitmap bantu = imageBitmap;

        if (bantu == null){
            Toast.makeText(getApplicationContext(),"Masukkan Gambar Terlebih Dahulu",Toast.LENGTH_SHORT).show();
        }else {
            Mat rgba = new Mat();
            Utils.bitmapToMat(bantu, rgba);

            Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
            Imgproc.Canny(edges, edges, 80, 100);

            bantu = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(edges, bantu);
            imageView.setImageBitmap(bantu);
            histogram();
        }
    }
    public void threshold(View view){

        Bitmap bantu = imageBitmap;

        if (bantu == null){
            Toast.makeText(getApplicationContext(),"Masukkan Gambar Terlebih Dahulu",Toast.LENGTH_SHORT).show();
        }else {
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bantu, imageMat);
            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(imageMat, imageMat, 120, 255, Imgproc.THRESH_BINARY);
            Utils.matToBitmap(imageMat, bantu);

            imageView.setImageBitmap(bantu);
            histogram();
        }
    }

    public void blur(View view){

        Bitmap bantu = imageBitmap;

        if (bantu == null){
            Toast.makeText(getApplicationContext(),"Masukkan Gambar Terlebih Dahulu",Toast.LENGTH_SHORT).show();
        }else {
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bantu, imageMat);
//            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(imageMat, imageMat, new Size(45,45),0);
            Utils.matToBitmap(imageMat, bantu);

            imageView.setImageBitmap(bantu);
            histogram();
        }
    }

    public void Invert(View view){

        Bitmap bantu = imageBitmap;

        if (bantu == null){
            Toast.makeText(getApplicationContext(),"Masukkan Gambar Terlebih Dahulu",Toast.LENGTH_SHORT).show();
        }else {
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bantu, imageMat);
            Imgproc.threshold(imageMat, imageMat, 120, 255, Imgproc.THRESH_BINARY_INV);
            Utils.matToBitmap(imageMat, bantu);

            imageView.setImageBitmap(bantu);
            histogram();
        }
    }
}
