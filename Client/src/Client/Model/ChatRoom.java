package Client.Model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class ChatRoom {
  private ObservableValue<Integer> id;
  private ObservableValue<Integer> clientCount;
  private SimpleStringProperty name;
  private SimpleStringProperty description;

  public ChatRoom(int id, int clientCount, String name, String description) {
    this.id = new SimpleObjectProperty<>(id);
    this.clientCount = new SimpleObjectProperty<>(clientCount);
    this.name = new SimpleStringProperty(name);
    this.description = new SimpleStringProperty(description);
  }

  public Integer getId() {
    return id.getValue();
  }

  public ObservableValue<Integer> idProperty() {
    return id;
  }

  public Integer getClientCount() {
    return clientCount.getValue();
  }

  public ObservableValue<Integer> clientCountProperty() {
    return clientCount;
  }

  public String getName() {
    return name.get();
  }

  public SimpleStringProperty nameProperty() {
    return name;
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public String getDescription() {
    return description.get();
  }

  public SimpleStringProperty descriptionProperty() {
    return description;
  }

  public void setDescription(String description) {
    this.description.set(description);
  }
}
