package zarvis.bakery.Gui;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import zarvis.bakery.Gui.controller.CustomerEditDialogController;
import zarvis.bakery.Gui.controller.CustomerOverviewController;
import zarvis.bakery.Gui.controller.OrdersController;
import zarvis.bakery.Gui.controller.RootLayoutController;
import zarvis.bakery.Gui.model.Customer;
import zarvis.bakery.Gui.model.CustomerListWrapper;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.utils.Util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;



public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    
    /**
     * The data as an observable list of Customers.
     */
    private ObservableList<Customer> customerData = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp() {
        // Add some sample data
    	BakeryJsonWrapper wrapper = Util.getWrapper();
    	for (zarvis.bakery.models.Customer customer : wrapper.getCustomers()) {
    		Customer c = new Customer();
    		c.setName(customer.getName());
    		c.setGuid(customer.getGuid());
    		c.setLocation(customer.getLocation());
    		c.setType(customer.getType());
    		c.setTotalType1(customer.getTotal_type1());
    		c.setTotalType2(customer.getTotal_type2());
    		c.setTotalType3(customer.getTotal_type3());
    		customerData.add(c);
    	}
    }

    /**
     * Returns the data as an observable list of Customers. 
     * @return
     */
    public ObservableList<Customer> getCustomerData() {
        return customerData;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("zarvis project");
        
        initRootLayout();
       
        showCustomerOverview();
    }

    /**
     * Initializes the root layout and tries to load the last opened
     * Customer file.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
                    .getResource("controller/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Try to load last opened Customer file.
        File file = getCustomerFilePath();
        if (file != null) {
            loadCustomerDataFromFile(file);
        }
    }

    /**
     * Shows the Customer overview inside the root layout.
     */
    public void showCustomerOverview() {
        try {
            // Load Customer overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("controller/CustomerOverview.fxml"));
            AnchorPane CustomerOverview = (AnchorPane) loader.load();

            // Set Customer overview into the center of root layout.
            rootLayout.setCenter(CustomerOverview);

            // Give the controller access to the main app.
            CustomerOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a dialog to edit details for the specified Customer. If the user
     * clicks OK, the changes are saved into the provided Customer object and true
     * is returned.
     * 
     * @param Customer the Customer object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showCustomerEditDialog(Customer Customer) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("controller/CustomerEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Customer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Customer into the controller.
            CustomerEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCustomer(Customer);
            
            // Set the dialog icon.
            //dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void showOrders(Customer Customer){
    	try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("controller/Orders.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("orders");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            // Set the Customer into the controller.
            OrdersController controller = loader.getController();
            //controller.setDialogStage(dialogStage);
            controller.setOrders(Customer);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            
        }
    }
    
    /**
     * Returns the Customer file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     * 
     * @return
     */
    public File getCustomerFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     * 
     * @param file the file or null to remove the path
     */
    public void setCustomerFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("Zarvis project - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("Zarvis project");
        }
    }
    
    /**
     * Loads Customer data from the specified file. The current Customer data will
     * be replaced.
     * 
     * @param file
     */
    public void loadCustomerDataFromFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(CustomerListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            CustomerListWrapper wrapper = (CustomerListWrapper) um.unmarshal(file);

            customerData.clear();
            customerData.addAll(wrapper.getCustomers());

            // Save the file path to the registry.
            setCustomerFilePath(file);

        } catch (Exception e) { // catches ANY exception
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Could not load data");
        	alert.setContentText("Could not load data from file:\n" + file.getPath());
        	
        	alert.showAndWait();
        }
    }

    /**
     * Saves the current Customer data to the specified file.
     * 
     * @param file
     */
    public void saveCustomerDataToFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(CustomerListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our Customer data.
            CustomerListWrapper wrapper = new CustomerListWrapper();
            wrapper.setCustomers(customerData);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

            // Save the file path to the registry.
            setCustomerFilePath(file);
        } catch (Exception e) { // catches ANY exception
        	Alert alert = new Alert(AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Could not save data");
        	alert.setContentText("Could not save data to file:\n" + file.getPath());
        	
        	alert.showAndWait();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}