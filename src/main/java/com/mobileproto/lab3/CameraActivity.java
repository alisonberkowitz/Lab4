package com.mobileproto.lab3;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by evan on 9/15/13.
 */
public class CameraActivity extends Activity {


    private static final int TAKE_PICTURE = 0;
    private Uri mUri;
    private Bitmap mPhoto;
    private boolean pictureTaken = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        final GPS gps = new GPS(this);

        Button take_picture = (Button) findViewById(R.id.takePicture);

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                File f = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                mUri = Uri.fromFile(f);
                startActivityForResult(i, TAKE_PICTURE);
                pictureTaken = true;

                //get coordinates of da pic
                if (pictureTaken){
                    if(gps.canGetLocation()){
                        TextView lat = (TextView) findViewById(R.id.lat);
                        TextView lon = (TextView) findViewById(R.id.lon);
                        lat.setText("Lattitude: " + String.valueOf(gps.getLatitude()));
                        lon.setText("Longitude: " + String.valueOf(gps.getLongitude()));
                        Log.v("Lattitude", String.valueOf(gps.getLatitude()));
                        new RemoteGeocode(){
                            @Override
                            public void onPostExecute(String result){
                                TextView xml = (TextView) findViewById(R.id.textView);
                               // xml.setText(result);
                            }
                        }.execute(RemoteGeocode.generateUri(gps.getLatitude(), gps.getLongitude()));
                        System.out.println("RECIEVED COORDINATES: " + lat + " , " + lon);
                    }
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (data != null){
                    if (resultCode != RESULT_CANCELED){
                        if (resultCode == Activity.RESULT_OK) {
                            getContentResolver().notifyChange(mUri, null);
                            ContentResolver cr = getContentResolver();
                            try {
                                mPhoto = android.provider.MediaStore.Images.Media.getBitmap(cr, mUri);
                                ((ImageView)findViewById(R.id.photoHolder)).setImageBitmap(mPhoto);

                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        }
    }
}
