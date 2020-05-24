
enum OrderStatus {
  NEW("blue"),
  PLACED("green"),
  SENT("yellow");

  private final String color;

  OrderStatus(String color) {
    this.color = color;
    int a = 1;
    a = 2; // Noncompliant
  }

  public String color() {
    return color;
  }
}
