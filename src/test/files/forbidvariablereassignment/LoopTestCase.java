
class LoopTestCase {

  {
    for (int i=0; i < 10; i++) System.out.println(i);

    for (int i=0; i < 10; i++) {
      System.out.println(i);
      i++; // Noncompliant
    }

    for (int x : Arrays.asList(1, 2, 3)) {
      System.out.println(x);
    }

    int i = 0;
    do {
      System.out.println(i);
      --i; // Noncompliant
    } while(i++ > -10);

    int k = 5;
    while (k++ < 30) {
      k++; // Noncompliant
      k += 2; // Noncompliant
      System.out.println(k);
    }
  }

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
