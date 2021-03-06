package Client.Controller;

import Client.Main;
import Client.Model.ChRoom;
import CustomClass.ChatRoom;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomListController {
  @FXML private ImageView refresh;
  @FXML private Pagination pagination;
  private static int dataSize;
  private TableView<ChRoom> table;
  private List<ChRoom> chatRoom;
  private static int rowsPerPage = 5;

  public void init(Object obj) {
    dataSize = ((List) obj).size();
    chatRoom = createData(obj);
    table = createTable();
    pagination.setPageFactory(this::createPage);
    pagination.setPageCount(dataSize / rowsPerPage + 1);
    pagination.setMaxPageIndicatorCount(10);
    addButtonToTable();
    Platform.runLater(()-> refresh.setImage(new Image(new File("src/Client/Assets/images/refresh.png").toURI().toString())));
  }

  //this method used to fill ChatRoom in tableview
  private List<ChRoom> createData(Object obj) {
    List<ChRoom> chatRoom = new ArrayList<>(dataSize);
    for (int i = 0; i < dataSize; i++) {
      ChatRoom object = (ChatRoom) ((List) obj).get(i);
      chatRoom.add(new ChRoom(i + 1, object.clientCount.get(), object.name, object.description));
    }
    return chatRoom;
  }

  private TableView<ChRoom> createTable() {

    TableView<ChRoom> table = new TableView<>();

    TableColumn<ChRoom, Integer> idColumn = new TableColumn<>("Id");
    idColumn.setCellValueFactory(param -> param.getValue().idProperty());
    idColumn.setPrefWidth(32);
    idColumn.setStyle("-fx-alignment: CENTER-LEFT;");

    TableColumn<ChRoom, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
    nameColumn.setPrefWidth(96);
    nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

    TableColumn<ChRoom, String> descColumn = new TableColumn<>("Description");
    descColumn.setCellValueFactory(param -> param.getValue().descriptionProperty());
    descColumn.setPrefWidth(305);
    descColumn.setStyle("-fx-alignment: CENTER-LEFT;");

    TableColumn<ChRoom, Integer> clientColumn = new TableColumn<>("Clients");
    clientColumn.setCellValueFactory(param -> param.getValue().clientCountProperty());
    clientColumn.setPrefWidth(79);
    clientColumn.setStyle("-fx-alignment: CENTER-LEFT;");

    table.getColumns().addAll(idColumn, nameColumn, descColumn, clientColumn);
    return table;
  }

  //method to create page inside pagination view
  private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, chatRoom.size());
    table.setItems(FXCollections.observableArrayList(chatRoom.subList(fromIndex, toIndex)));
    return table;
  }

  private void addButtonToTable() {
    TableColumn<ChRoom, Void> colBtn = new TableColumn("");

    Callback<TableColumn<ChRoom, Void>, TableCell<ChRoom, Void>> cellFactory = new Callback<>() {
      @Override
      public TableCell<ChRoom, Void> call(final TableColumn<ChRoom, Void> param) {
        final TableCell<ChRoom, Void> cell = new TableCell<>() {
          private Button btn = new Button("Join");

          {
            btn.setOnAction((ActionEvent event) -> {
              ChRoom chatRoom = getTableView().getItems().get(getIndex());
              int id = chatRoom.getId();
              System.out.println("selectedData: " + id);
              try {
                Main.client.request("joinRoom#" + (id - 1));
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
          }

          @Override
          public void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              setGraphic(btn);
            }
          }
        };
        return cell;
      }
    };
    colBtn.setCellFactory(cellFactory);
    table.getColumns().add(colBtn);
  }

  public void back() throws IOException {
    RootController rc = new RootController();
    rc.changeScene("root");
  }

  public void refreshTable() throws IOException {
    Main.client.request("roomls");
  }
}
