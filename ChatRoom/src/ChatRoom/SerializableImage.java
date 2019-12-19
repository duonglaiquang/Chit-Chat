package ChatRoom;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.Serializable;


public class SerializableImage implements Serializable {
  private int width, height;
  private int[][] data;

  public void setImage(Image image) {
    width = ((int) image.getWidth());
    height = ((int) image.getHeight());
    data = new int[width][height];

    PixelReader r = image.getPixelReader();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        data[i][j] = r.getArgb(i, j);
      }
    }

  }

  public Image getImage() {
    WritableImage img = new WritableImage(width, height);

    PixelWriter w = img.getPixelWriter();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        w.setArgb(i, j, data[i][j]);
      }
    }

    return img;
  }

}