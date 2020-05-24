
class ReportReassignmentInsideLoopTestCase {
  void kek (int x) {
    x = 5; // Noncompliant
    int b = 3;
    b = 2; // Noncompliant

    int bar;
    for (int i = 0; i < 100; i++) {
      i = 15; // Noncompliant
      int illAllowIt;
      illAllowIt = 2;
      bar = 2; // Noncompliant
    }
  }
}
