package Client.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageController {
  @FXML private ImageView largeImage;

  public void showImage(Image img){
    Platform.runLater(()->largeImage.setImage(img));
  }
}
