package alexparunov.steganographer.activities.encrypt;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import alexparunov.steganographer.algorithms.Embedding;

class EncryptInteractorImpl implements EncryptInteractor {

  private EncryptInteractorListener listener;

  EncryptInteractorImpl(EncryptInteractorListener listener) {
    this.listener = listener;
  }

  @Override
  public void performSteganography(String message, Bitmap coverImage, Bitmap secretImage,String Password) {
    if (secretImage == null && message != null) {
      new EmbedSecretMessage(message, coverImage, null,Password).execute();
    } else if(secretImage != null){
      new EmbedSecretMessage(null, coverImage, secretImage,Password).execute();
    } else {
      listener.onPerformSteganographyFailure();
    }
  }

  /**
   * AsyncTask which embeds secret message in the background not to block main frame
   */
  private class EmbedSecretMessage extends AsyncTask<Void, Void, Bitmap> {
    String message,password;
    Bitmap coverImage, secretImage;

    EmbedSecretMessage(String message, Bitmap coverImage, Bitmap secretImage,String Password) {
      this.message = message;
      this.coverImage = coverImage;
      this.secretImage = secretImage;
      this.password= Password;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
      Bitmap stegoImage = null;

      if (message != null && secretImage == null && coverImage != null) {
        stegoImage = Embedding.embedSecretText(this.message, this.coverImage,this.password);
      } else if (message == null && secretImage != null && coverImage != null) {
        stegoImage = Embedding.embedSecretImage(this.coverImage, this.secretImage,this.password);
      }

      return stegoImage;
    }

    @Override
    protected void onPostExecute(Bitmap stegoImage) {
      if (stegoImage != null) {
        listener.onPerformSteganographySuccessful(stegoImage);
      } else {
        listener.onPerformSteganographyFailure();
      }
    }
  }

  interface EncryptInteractorListener {

    /**
     * Listener which is invoked after successfully performed steganography
     * @param stegoImage Bitmap which is the result image of steganography
     */
    void onPerformSteganographySuccessful(Bitmap stegoImage);

    /**
     * Listener which is invoked after failure during steganography
     */
    void onPerformSteganographyFailure();
  }
}
