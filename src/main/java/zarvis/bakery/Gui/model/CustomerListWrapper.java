package zarvis.bakery.Gui.model;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "customers")
public class CustomerListWrapper {

    private List<Customer> customers;

    @XmlElement(name = "customers")
    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}