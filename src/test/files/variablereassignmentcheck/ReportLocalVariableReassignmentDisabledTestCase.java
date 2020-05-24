
class ReportLocalVariableReassignmentDisabledTestCase {

  void testMethod(int a) {
    a = 5; // Noncompliant
    int b = 1;
    b = 2;

    int c;
    c = 1;
    c = 2;
  }

}
