package alexparunov.steganographer.activities.decrypt;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import alexparunov.steganographer.R;
import alexparunov.steganographer.utils.Constants;
import alexparunov.steganographer.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DecryptActivity extends AppCompatActivity implements DecryptView {

  @BindView(R.id.ivStegoImage)
  ImageView ivStegoImage;
  @BindView(R.id.password)
  EditText password;

  @OnClick(R.id.ivStegoImage)
  public void onStegoImageClick() {
    if (ContextCompat.checkSelfPermission(getApplicationContext(),
      Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(DecryptActivity.this,
        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
        Constants.PERMISSIONS_EXTERNAL_STORAGE);

    } else {
      chooseImage();
    }

  }

  @OnClick(R.id.bDecrypt)
  public void onButtonClick() {
    String kunci = getpassword();
    if (!kunci.isEmpty()) {
      if (isSISelected) {
        if ((password.getText().toString().trim().length() <6))
        {
          mPresenter.decryptMessage(kunci);
        }
        else{
          showToast(R.string.password_kurang);
        }
      } else {
        showToast(R.string.stego_image_not_selected);
      }
    }
    else{
      showToast(R.string.password_text_empty);
    }
  }

  private ProgressDialog progressDialog;
  private DecryptPresenter mPresenter;
  private boolean isSISelected = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decrypt);

    ButterKnife.bind(this);

    progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Please wait...");

    mPresenter = new DecryptPresenterImpl(this);
    initToolbar();

  }

  @Override
  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Decryption");
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
      case Constants.PERMISSIONS_EXTERNAL_STORAGE:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          chooseImage();
        }
        break;
    }
  }

  @Override
  public void chooseImage() {
    Intent intent = new Intent(
      Intent.ACTION_PICK,
      android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    startActivityForResult(
      Intent.createChooser(intent, getString(R.string.choose_image)),
      Constants.SELECT_FILE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      if (requestCode == Constants.SELECT_FILE) {
        Uri selectedImageUri = data.getData();
        String tempPath = getPath(selectedImageUri, DecryptActivity.this);
        if(tempPath != null) {
          mPresenter.selectImage(tempPath);
        }
      }
    }
  }

  @Override
  public void startDecryptResultActivity(String secretMessage, String secretImagePath) {
    Intent intent = new Intent(DecryptActivity.this, DecryptResultActivity.class);

    if (secretMessage != null) {
      intent.putExtra(Constants.EXTRA_SECRET_TEXT_RESULT, secretMessage);
    }

    if (secretImagePath != null) {
      intent.putExtra(Constants.EXTRA_SECRET_IMAGE_RESULT, secretImagePath);
    }

    startActivity(intent);
  }

  public String getPath(Uri uri, Activity activity) {
    String[] projection = {MediaStore.MediaColumns.DATA};
    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }

  @Override
  public Bitmap getStegoImage() {
    return ((BitmapDrawable) ivStegoImage.getDrawable()).getBitmap();
  }

  @Override
  public void setStegoImage(File file) {
    showProgressDialog();
    Picasso.with(this)
      .load(file)
      .fit()
      .placeholder(R.mipmap.ic_launcher)
      .into(ivStegoImage);
    stopProgressDialog();
    isSISelected = true;
  }

  @Override
  public void showToast(int message) {
    StandardMethods.showToast(this, message);
  }

  @Override
  public void showProgressDialog() {
    if (progressDialog != null && !progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override
  public void stopProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public String getpassword() {
    return password.getText().toString();
  }
}
