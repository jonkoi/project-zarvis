package zarvis.bakery.Gui.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import zarvis.bakery.Gui.MainApp;
import zarvis.bakery.Gui.model.Customer;


public class CustomerOverviewController {
    @FXML
    private TableView<Customer> CustomerTable;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> guidColumn;
    @FXML
    private Label nameLabel;
    @FXML
    private Label guidLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label totalType1Label;
    @FXML
    private Label totalType2Label;
    @FXML
    private Label totalType3Label;
    @FXML
    private Label locationLabel;
    
    // Reference to the main application.
    private MainApp mainApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public CustomerOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the Customer table with the two columns.
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        guidColumn.setCellValueFactory(cellData -> cellData.getValue().guidProperty());
        
        // Clear Customer details.
        showCustomerDetails(null);

        // Listen for selection changes and show the Customer details when changed.
        CustomerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCustomerDetails(newValue));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
        CustomerTable.setItems(mainApp.getCustomerData());
    }
    
    /**
     * Fills all text fields to show details about the Customer.
     * If the specified Customer is null, all text fields are cleared.
     * 
     * @param Customer the Customer or null
     */
    private void showCustomerDetails(Customer Customer) {
        if (Customer != null) {
            // Fill the labels with info from the Customer object.
            nameLabel.setText(Customer.getName());
            guidLabel.setText(Customer.getGuid());
            typeLabel.setText(Customer.getType());
            totalType1Label.setText(Integer.toString(Customer.getTotalType1()));
            totalType2Label.setText(Integer.toString(Customer.getTotalType2()));
            totalType3Label.setText(Integer.toString(Customer.getTotalType3()));
            locationLabel.setText("x = "+Customer.getLocation().getX()+ ", y = "+Customer.getLocation().getY());

        } else {
            // Customer is null, remove all the text.
            nameLabel.setText("");
            guidLabel.setText("");
            typeLabel.setText("");
            totalType1Label.setText("");
            totalType1Label.setText("");
            totalType1Label.setText("");
            locationLabel.setText("");

        }
    }
    
    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeleteCustomer() {
        int selectedIndex = CustomerTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            CustomerTable.getItems().remove(selectedIndex);
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer in the table.");
            
            alert.showAndWait();
        }
    }
    
    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new Customer.
     */
    @FXML
    private void handleNewCustomer() {
        Customer tempCustomer = new Customer();
        boolean okClicked = mainApp.showCustomerEditDialog(tempCustomer);
        if (okClicked) {
            mainApp.getCustomerData().add(tempCustomer);
        }
    }
    
    @FXML
    private void handleOrders() {
    	Customer selectedCustomer = CustomerTable.getSelectionModel().getSelectedItem();
    	 if (selectedCustomer != null) {
            mainApp.showOrders(selectedCustomer);

         } else {
             // Nothing selected.
             Alert alert = new Alert(AlertType.WARNING);
             alert.initOwner(mainApp.getPrimaryStage());
             alert.setTitle("No Selection");
             alert.setHeaderText("No Customer Selected");
             alert.setContentText("Please select a Customer in the list.");
             
             alert.showAndWait();
         }
    	
    }
    
    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected Customer.
     */
    @FXML
    private void handleEditCustomer() {
        Customer selectedCustomer = CustomerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            boolean okClicked = mainApp.showCustomerEditDialog(selectedCustomer);
            if (okClicked) {
                showCustomerDetails(selectedCustomer);
            }

        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer in the table.");
            
            alert.showAndWait();
        }
    }
}