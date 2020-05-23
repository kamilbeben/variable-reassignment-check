class BasicTestCase {

  private int field1 = 2;
  private int field2;

  {

    @CustomMutable int mutableVariable1 = 1;
    mutableVariable1 = 11;

    @CustomMutable
    int mutableVariable2 = 2;
    mutableVariable2 = 21;

    @CustomMutable




    int mutableVariable3;

    while (c) {
      mutableVariable3 = 3;
    }
  }

  {
    field1 = 3;
    field2 = 4;

    int local1;
    int local2;

    if (c) local1 = 1;
    else local1 = 2;

    for (;;) field1 = 1;
    for (;;) {
      field2 = 2;
      local1 = 1; // Noncompliant
      local2 = 1; // Noncompliant
    }
  }

  void blockCases() {
    int a;
    {
      a = 2;
    }
    a = 3; // Noncompliant

    int b;

    try {
      a = 2; // Noncompliant
      b = 1;
    } catch (Exception e) {
      b = 1; // Noncompliant
    }

    {
      int c = 1;
    }
    int c;
    {
      c = 2;
    }
  }

  void basicCases() {
    int k = 4;
    int z = 2;
    new Object() {
      void kk() {
        int k;
        k = 3; // it's okay because it is in different scope
      }
    };

    field1 = 1; // it's okay, we are interested in local variables only

    z = 5; // Noncompliant

    z = c // Noncompliant
      ? 1
      : 2;

    z = c ? 1 : 2;  // Noncompliant

    int err6 = 1;
    err6++;    // Noncompliant
    err6--;    // Noncompliant
    ++err6;    // Noncompliant
    --err6;    // Noncompliant
    err6 += 1; // Noncompliant
    err6 -= 1; // Noncompliant
    err6 *= 1; // Noncompliant
    err6 /= 1; // Noncompliant
  }

}
