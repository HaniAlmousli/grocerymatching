package com.hanialmousli.shoppingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.loopj.android.http.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    private Button btnSelect;
//    private ImageView ivImage;
    private String pictureImagePath = "";
    private String REST_TYPE_ADDRESS="http://23.233.47.6/groceries/api/v1.0/getsimilar";

    // Items used for filling the list
    ListView productsList;
    ArrayList<String> itemname = new ArrayList<String>();
    ArrayList<Bitmap> itemImg= new ArrayList<Bitmap>();
    CustomListAdapter adapter;

    // pricing items
    Double totalCAD=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TO IGNORE FILEURIEXPOSE
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean result=Utility.checkPermission(MainActivity.this);
                if(result)
                    cameraIntent();
            }
        });

        adapter=new CustomListAdapter(this, itemname, itemImg);
        productsList=(ListView)findViewById(R.id.products_list);
        productsList.setAdapter(adapter);

        ((EditText)findViewById(R.id.lblPrice)).setEnabled(false);
        ((EditText)findViewById(R.id.lblPrice)).setFocusable(false);
//        ivImage = (ImageView) findViewById(R.id.ivImage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraIntent();
                    break;
                }
        }
    }

    private void cameraIntent()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);// /sdcard/Pictures
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {

        sendImageREST();
        File imgFile = new  File(pictureImagePath);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
//            ivImage.setImageBitmap(myBitmap);
            itemname.add("");
            itemImg.add(myBitmap);
//            this.adapter.notifyDataSetChanged();
        }
    }

    private void sendImageREST() {

        File myPic = new  File(pictureImagePath);
        int size = (int) myPic.length();
        byte buffer[]= new byte[size];
        FileInputStream fis = null;
        AUXCLASS.IMAGE_TYPE="ERROR";
        try {
            fis = new FileInputStream(myPic);
            fis.read(buffer);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.toString());
        } catch (IOException e) {
            System.out.println("Exception reading file: " + e.toString());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ignored) {
            }
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("file", new ByteArrayInputStream(buffer), "img.jpg");
        client.post(REST_TYPE_ADDRESS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                // called when response HTTP status is "200 OK"
                try {
                    String type= res.getString("type");
                    Double price= Double.parseDouble(res.getString("price"));
                    totalCAD +=price;
                    itemname.set(itemname.size()-1,type+'\n'+price.toString()+" CAD");
                    adapter.notifyDataSetChanged();
                    ((EditText)findViewById(R.id.lblPrice)).setText(totalCAD.toString()+ " CAD");
//                    AUXCLASS.MESSAGEBOXSHOW(AUXCLASS.IMAGE_TYPE ,"Type",MainActivity.this);
                } catch (JSONException e) {
                    itemname.remove(itemname.size()-1);
                    itemImg.remove(itemImg.size()-1);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                itemname.remove(itemname.size()-1);
                itemImg.remove(itemImg.size()-1);
                AUXCLASS.MESSAGEBOXSHOW("Error Happened !","ERROR",MainActivity.this);
            }

        });
    }

}

