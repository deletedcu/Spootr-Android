package com.ottawa.spootr2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.baoyz.actionsheet.ActionSheet;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ottawa.spootr2.R;
import com.ottawa.spootr2.common.Constants;
import com.ottawa.spootr2.common.KeyUtil;
import com.ottawa.spootr2.common.SharedData;
import com.ottawa.spootr2.model.Post;

import net.louislam.android.L;

import org.apache.http.Header;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by king on 24/01/16.
 */
public class PostActivity extends AppCompatActivity implements ActionSheet.ActionSheetListener {

    private EditText editText;
    private ImageView imageView;
    private SharedPreferences preferences;
    private Post post;
    private ImageButton sendButton;
    private ImageButton backButton;
    private ImageButton photoButton;
    private ImageButton deleteButton;
    private ProgressDialog loadingDialog;
    private boolean isEdit;
    private boolean isAttached;
    private boolean isDeleted;
    private final String TAG = "ImageChooser";
    private String imageName;
    private String directoryPath;
    private int imageWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initComponent();
        initData();

    }

    /***********************************************************************************************
     ***********************                Internal Methods                ************************
     **********************************************************************************************/

    private void initData() {
        preferences = getSharedPreferences(Constants.PREFS_NAME, 0);

        File rootSD = Environment.getExternalStorageDirectory();
        File directory = new File(rootSD.getAbsolutePath() + "/spootr/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        directoryPath = directory.getPath();

        imageWidth = SharedData.getInstance().image_width;

        imageView.getLayoutParams().width = imageWidth;
        imageView.getLayoutParams().height = imageWidth;
        deleteButton.setVisibility(View.GONE);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            post = (Post)extra.getSerializable("post");
        }

        if (post != null) {
            isEdit = true;
            editText.setText(post.getStrContent());
            if (!post.getStrImageName().equals("")) {
                String strURL = String.format("%s%s", Constants.DOWNLOAD_URL, post.getStrImageName());
                UrlImageViewHelper.setUrlDrawable(imageView, strURL, R.drawable.spootr);
                deleteButton.setVisibility(View.VISIBLE);
            }
        } else {
            isEdit = false;
        }

        checkSendButtonState();
    }

    private void initComponent() {
        setContentView(R.layout.activity_post);


        editText = (EditText)findViewById(R.id.text_post_content);
        imageView = (ImageView)findViewById(R.id.imageView_add_post);
        backButton = (ImageButton)findViewById(R.id.button_post_back);
        sendButton = (ImageButton)findViewById(R.id.button_post_send);
        photoButton = (ImageButton)findViewById(R.id.button_post_photo);
        deleteButton = (ImageButton)findViewById(R.id.button_post_delete);
        deleteButton.setVisibility(View.GONE);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyUtil.hideSoftKeyboard(PostActivity.this);
                return false;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyUtil.hideSoftKeyboard(PostActivity.this);
                clearFiles();
                finish();
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionSheet();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(null);
                deleteButton.setVisibility(View.GONE);
                imageName = "";
                isAttached = false;
                isDeleted = true;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEdit) {
                    editPost();
                } else {
                    addPost();
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkSendButtonState();
            }
        });

    }

    public void showActionSheet() {
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("Cancel")
                .setOtherButtonTitles("Take Photo", "Show Gallery")
                .setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            takePhoto();
        } else if (index == 1) {
            showGallery();
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {

    }



    private void checkSendButtonState() {

        boolean isEnabled;

        String text = editText.getText().toString().trim();
        if (text.equals("")) {
            isEnabled = false;
        } else {
            isEnabled = true;
        }

        sendButton.setEnabled(isEnabled);

    }

    private void takePhoto() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.REQUEST_TAKE_PICTURE);
    }

    private void showGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.REQUEST_SELECT_PICTURE);
    }

    private void performCrop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);

        startActivityForResult(intent, Constants.REQUEST_CROP_IMAGE);
    }

    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File file = new File(Environment.getExternalStorageDirectory(), "temp.jpeg");
            try {
                file.createNewFile();
            } catch (IOException e) {}

            return file;
        } else {

            return null;
        }
    }

    private Bitmap getBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 300, 300);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_TAKE_PICTURE) {
                Uri uri = data.getData();
                performCrop(uri);
            }
            else if (requestCode == Constants.REQUEST_CROP_IMAGE || requestCode == Constants.REQUEST_SELECT_PICTURE) {
                try {
                    File tempFile = getTempFile();
                    Bitmap bitmap = getBitmap(tempFile.getPath());

                    int nUserId = preferences.getInt(Constants.USER_ID, 0);
                    Date date = new Date();
                    imageName = nUserId + "_" + date.getTime();
                    String filePath = directoryPath + "/" + imageName;
                    File file = new File(filePath);
                    FileOutputStream outputStream = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESS_RATE, outputStream);
                    outputStream.close();

                    imageView.setImageBitmap(bitmap);

                    if (tempFile.exists()) tempFile.delete();

                    isAttached = true;
                    isDeleted = true;
                    deleteButton.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***********************************************************************************************
     **********************                  Web Api Functions               ***********************
     **********************************************************************************************/

    /**
     * api method: addPost
     * parameter: post_id, content, is_attached, is_deleted
     * return: json(status)
     */
    private void addPost() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();

        int nUserId = preferences.getInt(Constants.USER_ID, 0);
        final String content = editText.getText().toString().trim();
        float latitude = preferences.getFloat(Constants.LATITUDE, 0);
        float longitude = preferences.getFloat(Constants.LONGITUDE, 0);

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "addPost");
        params.put(Constants.USER_ID, nUserId);
        params.put(Constants.CONTENT, content);
        params.put("is_attached", isAttached ? 1 : 0);
        params.put(Constants.LATITUDE, latitude);
        params.put(Constants.LONGITUDE, longitude);

        if (isAttached) {
            try {
                String filePath = directoryPath + "/" + imageName;
                File file = new File(filePath);
                params.put("upload", file, "image/jpg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sharedData.httpClient.post(PostActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        setResult(RESULT_OK);
                        clearFiles();
                        finish();
                    } else if (status.equals("300")) {
                        L.alert(PostActivity.this, "Image size should be less than 2M.");
                    } else if (status.equals("400")) {
                        L.alert(PostActivity.this, "Upload was failed. Please try again.");
                    } else {
                        L.alert(PostActivity.this, Constants.WEB_FAILED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(PostActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(PostActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    /**
     * api method: editPost
     * parameter: post_id, content, circle_id, is_attached, is_deleted
     * return: json(status)
     */
    private void editPost() {
        loadingDialog = L.progressDialog(this, Constants.REQUEST_WAITING);
        SharedData sharedData = SharedData.getInstance();

        final String content = editText.getText().toString().trim();

        RequestParams params = new RequestParams();
        params.put(Constants.ACTION, "editPost");
        params.put(Constants.POST_ID, post.getnId());
        params.put(Constants.CONTENT, content);
        params.put("is_attached", isAttached ? 1 : 0);
        params.put("is_deleted", isDeleted ? 1 : 0);
        if (isAttached) {
            try {
                String filePath = directoryPath + "/" + imageName;
                File file = new File(filePath);
                params.put("upload", file, "image/jpg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        sharedData.httpClient.post(PostActivity.this, Constants.WEBSERVICE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] response) {
                loadingDialog.dismiss();
                try {
                    String strResponse = new String(response, "UTF-8");
                    JSONArray jsonArray = new JSONArray(strResponse);
                    String status = jsonArray.getJSONObject(0).getString(Constants.STATUS);
                    if (status.equals(Constants.STATUS_SUCCESS)) {
                        Post item = post;

                        item.setStrContent(content);
                        if (isDeleted) {
                            if (isAttached) {
                                item.setStrImageName(imageName);
                            } else {
                                item.setStrImageName("");
                            }
                        }

                        Intent intent = new Intent();
                        intent.putExtra("post", item);
                        setResult(RESULT_OK, intent);
                        clearFiles();
                        finish();
                    } else if (status.equals("300")) {
                        L.alert(PostActivity.this, "Image size should be less than 2M.");
                    } else if (status.equals("400")) {
                        L.alert(PostActivity.this, "Upload was failed. Please try again.");
                    } else {
                        L.alert(PostActivity.this, Constants.WEB_FAILED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    L.alert(PostActivity.this, Constants.WEB_FAILED);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                loadingDialog.dismiss();
                L.alert(PostActivity.this, Constants.WEB_FAILED);
            }
        });
    }

    private void clearFiles() {
        File dir = new File(directoryPath);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

}
