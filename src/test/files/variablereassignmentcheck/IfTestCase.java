
class IfTestCase {

  void compliantCasesWithConditionalStatements() {
    int ok1;
    if (c) {
      ok1 = 1;
    } else {
      ok1 = 2;
    }

    final int ok2 = 3;

    Car ok3;
    if (a) {
      if (b) {
        ok3 = new Car("blue");
      } else ok3 = new Car("weird i know");
    } else {
      ok3 = new Car("red");
    }

    int ok4;
    if (c) {
      if (d) {
        ok4 = 1;
      } else if (e) {
        ok4=2;
      } else if (f) ok4 = 3;
      else {
        ok4 = 4;
      }
    } else {
      if (g) ok4 = 5;
      else ok4 = 6;
      // ok4 = 7; // commented on purpose
    }

    if (c) {
      var k = 1;
    } else {
      var k = 2;
    }

    k = 3; // it should not see this variable

    for (int ok5 = 1; ok5 < 10; ok5++) { } // fori arguments are an exception

  }

  void nonCompliantCasesWithConditionalStatements() {
    int err1 = 5;
    err1 = 6; // Noncompliant

    int err2 = 5;
    if (c) {
      err2 = 3; // Noncompliant
    }
    err2 = 4; // Noncompliant

    var err4 = c ? 5 : 3;
    err4 = 2; // Noncompliant


    int err5;
    if (c) {
      if (d) {
        err5 = 4;
      }
      err5 = 5; // Noncompliant
    } else {
      err5 = 6;
    }

    /*
     * Forgive me whoever you are,
     * but i have to test it all.
     */

    String err9 = "a"; if (a) err9 = "c"; // Noncompliant
    else err9 = "d"; // Noncompliant

    err4 = "No no no no"; // Noncompliant

    short err10;
    if (c) err10 = 1;
    else err10 = 2;
  }

}
