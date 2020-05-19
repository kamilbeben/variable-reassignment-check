
class LoopTestCase {

  void whileTestCase() {

    int a;
    for (int i=0; i < 10; i++) {
      a = 1; // Noncompliant
    }

    Kind kind;
    for (Kind it : kinds) {

      kind = it; // Noncompliant

      if (c) {
        kind = it; // Noncompliant
      }

      if (c) kind = it; // Noncompliant
    }

    int k;
    while (c) {
      k = 1; // Noncompliant
    }
  }

}
