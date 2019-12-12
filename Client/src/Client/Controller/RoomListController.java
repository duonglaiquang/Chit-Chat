package Client.Controller;

import ChatRoom.ChatRoom;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class RoomListController {
  @FXML private TableColumn id;
  @FXML private TableView paginationTableView;
  @FXML private Pagination pagination;

  public List<ChatRoom> getTableData(){
    List<ChatRoom> data = new ArrayList<>();
    return data;
  }
}
