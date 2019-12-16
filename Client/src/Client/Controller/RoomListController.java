package Client.Controller;

import Client.Model.ChatRoom;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RoomListController implements Initializable {
  private final static int dataSize = 100;
  @FXML private Pagination pagination;
  private final TableView<ChatRoom> table = createTable();
  private final List<ChatRoom> ChatRoom = createData();
  private final static int rowsPerPage = 10;


  private TableView<ChatRoom> createTable() {

    TableView<ChatRoom> table = new TableView<>();

    TableColumn<ChatRoom, Integer> idColumn = new TableColumn<>("Id");
    idColumn.setCellValueFactory(param -> param.getValue().idProperty());
    idColumn.setPrefWidth(32);

    TableColumn<ChatRoom, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
    nameColumn.setPrefWidth(96);

    TableColumn<ChatRoom, String> descColumn = new TableColumn<>("Description");
    descColumn.setCellValueFactory(param -> param.getValue().descriptionProperty());
    descColumn.setPrefWidth(305);

    TableColumn<ChatRoom, Integer> clientColumn = new TableColumn<>("Clients");
    clientColumn.setCellValueFactory(param -> param.getValue().clientCountProperty());
    clientColumn.setPrefWidth(79);

    table.getColumns().addAll(idColumn, nameColumn, descColumn, clientColumn);
    return table;
  }

  //this method used to fill ChatRoom in tableview
  private List<ChatRoom> createData() {
    List<ChatRoom> ChatRoom = new ArrayList<>(dataSize);
    for (int i = 1; i <= dataSize; i++) {
      ChatRoom.add(new ChatRoom(i, i, "foo " + i, "bar " + i));
    }

    return ChatRoom;
  }


  @Override
  public void initialize(URL url, ResourceBundle rb) {
    pagination.setPageFactory(this::createPage);
    addButtonToTable();
  }

  //method to create page inside pagination view
  private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, ChatRoom.size());
    table.setItems(FXCollections.observableArrayList(ChatRoom.subList(fromIndex, toIndex)));
    return table;
  }

  private void addButtonToTable() {
    TableColumn<ChatRoom, Void> colBtn = new TableColumn("");

    Callback<TableColumn<ChatRoom, Void>, TableCell<ChatRoom, Void>> cellFactory = new Callback<>() {
      @Override
      public TableCell<ChatRoom, Void> call(final TableColumn<ChatRoom, Void> param) {
        final TableCell<ChatRoom, Void> cell = new TableCell<>() {
          private Button btn = new Button("Join");
          {
            btn.setOnAction((ActionEvent event) -> {
//              ChatRoom ChatRoom = getTableView().getItems().get(getIndex());
//              System.out.println("selectedData: " + ChatRoom);
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
}
