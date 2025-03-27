import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class CustomersManagementSystem {
    private Queue<String> generalQueue;
    private Stack<String> vipStack;
    private int totalGeneralServed;
    private int totalVipServed;

    public CustomersManagementSystem() {
        generalQueue = new LinkedList<>();
        vipStack = new Stack<>();
        totalGeneralServed = 0;
        totalVipServed = 0;
    }

    public void addGeneralCustomer(String name) {
        generalQueue.add(name);
    }

    public void addVipCustomer(String name) {
        vipStack.push(name);
    }

    public void serveVipCostumer() {
        if (!vipStack.isEmpty()) {
            System.out.println("Serving VIP customer: " + vipStack.pop());
            totalVipServed++;
        } else {
            System.out.println("No VIP customer to serve.");
        }
    }

    public void serveGeneralCostumer() {
        if (!generalQueue.isEmpty()) {
            System.out.println("Serving general customer: " + generalQueue.poll());
            totalGeneralServed++;
        } else {
            System.out.println("No general customer to serve.");
        }
    }

    public void displayReport() {
        System.out.println("Total VIP customers served: " + totalVipServed);
        System.out.println("Total general customers served: " + totalGeneralServed);
    }

}
