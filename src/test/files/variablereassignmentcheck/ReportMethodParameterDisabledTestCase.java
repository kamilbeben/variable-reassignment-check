
class ReportMethodParameterDisabledTestCase {

  void kek(int foo) {
    foo = 4;
    foo = 5;
    char x = 'a';
    x = 'c'; // Noncompliant
  }

}
