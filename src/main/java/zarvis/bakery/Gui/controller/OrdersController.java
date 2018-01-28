package zarvis.bakery.Gui.controller;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import zarvis.bakery.Gui.model.Customer;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;


public class OrdersController implements Initializable {
  
  @FXML
  private Button BtnDelete;
  
  @FXML 
  private HBox HBox4Btns; 

  @FXML 
  private ListView<String> listBoxMain;

  @FXML 
  private Label TitleLbl; 
  @FXML 
  private Label guidLabel;
  @FXML 
  private Label customerIdLabel; 
  @FXML 
  private Label productLabel; 
  @FXML 
  private VBox VBoxMain;

  @FXML
  private TextField txtAddItem; 
  
  
  final ObservableList<String> listItems = FXCollections.observableArrayList();        
  
  @FXML
  private void deleteAction(ActionEvent action){
   int selectedItem = listBoxMain.getSelectionModel().getSelectedIndex();
    listItems.remove(selectedItem);    
  }

  @FXML
  private void details(ActionEvent action){
	  String selectedItem = listBoxMain.getSelectionModel().getSelectedItem();
	  Order order = Util.getWrapper().getOrderById(selectedItem);
	  guidLabel.setText("Order Id: "+order.getGuid());
	  customerIdLabel.setText("Customer Id: "+order.getCustomer_id());
	  productLabel.setText("Product :"+order.getProducts().toString());
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    
	// TODO
    listBoxMain.setItems(listItems);
    
    // Disable buttons to start
    BtnDelete.setDisable(true);
  

    // Add a ChangeListener to ListView to look for change in focus
    listBoxMain.focusedProperty().addListener(new ChangeListener<Boolean>() {
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(listBoxMain.isFocused()){
          BtnDelete.setDisable(false);
        }
     }
    });    
    
  }


  	public void setOrders(Customer customer) {
  		
  		for(Order order : customer.getOrders()){
			listItems.add(order.getGuid());
			
		}
  	}  
}