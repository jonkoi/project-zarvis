package zarvis.bakery.Gui.controller;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import zarvis.bakery.Gui.model.Customer;
import zarvis.bakery.models.Location;


/**
 * Dialog to edit details of a Customer.

 */
public class CustomerEditDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField guidField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField totalType1Field;
    @FXML
    private TextField totalType2Field;
    @FXML
    private TextField totalType3Field;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    

    private Stage dialogStage;
    private Customer customer;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        
        // Set the dialog icon.
        //this.dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));
    }

    /**
     * Sets the Customer to be edited in the dialog.
     * 
     * @param Customer
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;

        // Fill the labels with info from the Customer object.
        nameField.setText(customer.getName());
        guidField.setText(customer.getGuid());
        typeField.setText(customer.getType());
        totalType1Field.setText(Integer.toString(customer.getTotalType1()));
        totalType2Field.setText(Integer.toString(customer.getTotalType2()));
        totalType3Field.setText(Integer.toString(customer.getTotalType3()));
        xField.setText(Float.toString(customer.getLocation().getX()));
        yField.setText(Float.toString(customer.getLocation().getY()));
        
        
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * 
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            customer.setName(nameField.getText());
            customer.setGuid(guidField.getText());
            customer.setType(typeField.getText());
            customer.setTotalType1(Integer.parseInt(totalType1Field.getText()));
            customer.setTotalType2(Integer.parseInt(totalType2Field.getText()));
            customer.setTotalType3(Integer.parseInt(totalType3Field.getText()));
            Location loc = new Location();
            loc.setX(Float.parseFloat(xField.getText()));
            loc.setY(Float.parseFloat(yField.getText()));
            customer.setLocation(loc);

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     * 
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "No valid name!\n"; 
        }
        if (guidField.getText() == null || guidField.getText().length() == 0) {
            errorMessage += "No valid  guid!\n"; 
        }
        if (typeField.getText() == null || typeField.getText().length() == 0) {
            errorMessage += "No valid type!\n"; 
        }

        if (totalType1Field.getText() == null || totalType1Field.getText().length() == 0) {
            errorMessage += "No valid total type 1!\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(totalType1Field.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid total type 1 (must be an integer)!\n"; 
            }
        }
        
        if (totalType2Field.getText() == null || totalType2Field.getText().length() == 0) {
            errorMessage += "No valid total type 2!\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(totalType2Field.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid total type 2 (must be an integer)!\n"; 
            }
        }
        
        if (totalType3Field.getText() == null || totalType3Field.getText().length() == 0) {
            errorMessage += "No valid total type 3!\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Integer.parseInt(totalType3Field.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid total type 3 (must be an integer)!\n"; 
            }
        }
        if (xField.getText() == null || xField.getText().length() == 0) {
            errorMessage += "No valid location.X !\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Float.parseFloat(xField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid location.X (must be an float)!\n"; 
            }
        }
        if (yField.getText() == null || yField.getText().length() == 0) {
            errorMessage += "No valid location.Y !\n"; 
        } else {
            // try to parse the postal code into an int.
            try {
                Float.parseFloat(yField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid location.Y (must be an float)!\n"; 
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            
            alert.showAndWait();
            
            return false;
        }
    }
}