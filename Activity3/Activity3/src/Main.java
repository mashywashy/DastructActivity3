public class Main {
    public static void main(String[] args) {
        CustomersManagementSystem system = new CustomersManagementSystem();

        system.addGeneralCustomer("Alice");
        system.addGeneralCustomer("Bob");
        system.addVipCustomer("Charlie");
        system.addVipCustomer("David");


        system.serveGeneralCostumer();
        system.serveVipCostumer();
        system.serveGeneralCostumer();
        system.serveVipCostumer();

        system.displayReport();
    }
}