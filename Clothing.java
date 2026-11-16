package smart.store;

public class Clothing extends Product {
    private String size;
    private String color;
    private String material;

    public Clothing(int id, String name, double price, int quantity,
                    String description, boolean isActive,
                    String size, String color, String material) {
        super(id, name, price, quantity, description, isActive);
        this.size = size;
        this.color = color;
        this.material = material;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    @Override
    public String getProductType() {
        return "Clothing";
    }

    @Override
    public String getDetailsList() {
        return "ID: " + getId()
                + " | Name: " + getName()
                + " | Size: " + size
                + " | Color: " + color
                + " | Material: " + material
                + " | Price: " + getFormattedPrice()
                + " | Available Qty: " + getQuantity();
    }
}