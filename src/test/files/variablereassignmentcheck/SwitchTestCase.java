
class SwitchTestCase {

  void uglyAsHellButPrettyStandard () {

    int err1;
    switch (status) {
      case NEW:
        err1 = 1;
        break;
      case CONFIRMED:
        err1 = 2;
        break;
      case  SENT:
        err1 = 3;
        err1 = 32; // Noncompliant
        break;
      case CUSTOMER_WAS_HIT_BY_A_CAR:
        err1 = 4;
      case ARCHIVED:
        err1 = 5; // Noncompliant
        break;
      default:
        err1 = 6;
        break;
    }

    int err2;
    switch (c) {
      case 0:
        System.out.println("test");
        err2 = 1;
        break;
      case 1:
        err2 = 2;
      case 2:
        err2 = 3; // Noncompliant
        break;
      case 3:
      case 31:
        err2 = 45;
        break;
      case 4:
        err2 = 6;
        return;
      default:
        err2 = 4;
    }
    err2 = 5; // Noncompliant

    TypeDoesNotMatter err3 = 42;
    switch (dunno) {
      case "a":
        err3 = 1; // Noncompliant
    }

    int err4;
    if (c) {
      switch (x) {
        case 1:
          err4 = 1;
          break;
        case 2:
          err4 = 5;
        default:
          return;
      }
    } else {
      err4 = 4;
    }

    int ok1;
    switch (outer) {
      case FIRST:
        switch (inner) {
          case FIRST_INNER:
            ok1 = 23;
            return;
          case SECOND_INNER:
            ok1 = 24;
            return;
        }
        break;
      case SECOND:
        ok1 = 30;
        return;
    }
  }
}
