package dk.zbc.selfie;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Images.Media.insertImage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    static final int REQUEST_IMAGE_CAPTURE = 1;
    /**
     * element declarations
      */
    public ImageView takenPictureView;
    public Button takePictureButton, savePictureButton;
    public String currentPhotoPath;
    public Intent takePictureIntent;
    public Bitmap photoBitMap;

    /**
     * onCreate method gets executed as the application is starting
     * in here we set the elements of the design to their corresponding ids.
     * and map methods to buttons.
     *
     * @author Mathias Kamp
     * @version 2022.04.26
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = findViewById(R.id.Btn_TakePicture);
        takenPictureView = findViewById(R.id.Iv_selfie);
        savePictureButton = findViewById(R.id.Btn_SavePicture);
        takePictureButton.setOnClickListener(view -> dispatchTakePictureIntent());
        savePictureButton.setOnClickListener(view -> galleryAddPic());

    }

    /**
     * dispatchTakePictureIntent has the purpose of creating an intent which opens the camera of the phone,
     * takes a picture and stores it as a .jpg file on the location -> Android/data/dk.zbc.selfie/files/Pictures
     *
     * @author Mathias Kamp
     * @version 2022.04.26
     *
     */
    private void dispatchTakePictureIntent() {

        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {


            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException e) {
                Log.e("take picture error", "dispatchTakePictureIntent: " + e);
            }

            if (photoFile != null) {

                Log.w(TAG, "dispatchTakePictureIntent: photoFile");

                Uri photoURI = FileProvider.getUriForFile(this,
                        "dk.zbc.selfie.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * handles the activity result of dispatchPictureIntent activity
     *
     * this method handles the result from dispatchPictureIntent activity and shows the pictures bitmap in an imageView
     *
     * @author Mathias Kamp
     * @version 2022.04.26
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null){

            setPicture();

        }
    }

    /**
     * creates a .jpg file of the image that you have taken
     * @return a file representing the image taken
     *
     * @throws IOException
     */

    private File createImageFile() throws IOException{

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
         File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     *method adds the taken picture to the gallery application
     * @author Mathias Kamp
     * @version 2022.04.26
     *
     */
    private void galleryAddPic(){

        File photoFile = new File(currentPhotoPath);
        insertImage(getContentResolver(),photoBitMap,photoFile.getName(), "selfie app photo");
    }

    /**
     * method sets the imageview with the taken pictures bitmap and also takes care of the scaling of the image
     * @author Mathias Kamp
     * @version 2022.04.26
     */
    private void setPicture(){
        int targetW = takenPictureView.getWidth();
        int targetH = takenPictureView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        photoBitMap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        takenPictureView.setImageBitmap(photoBitMap);

    }
}