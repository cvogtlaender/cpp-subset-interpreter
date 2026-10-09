// Name lookup order: local -> own members -> inherited members -> global,
// define-after-use for functions and classes, and C++ name hiding.
int value() { return 1; }
int helper(int x) { return x * 100; }

class Base {
public:
  int value;
  Base() { value = 2; }

  int helper(int x) { return x + 1; }
  int helper(char c) { return 0; }

  int useHelper() {
    // member function hides the global one
    return helper(5);
  }
};

class Derived : public Base {
public:
  Derived() { }

  int readValue() {
    // inherited field hides the global function
    return value;
  }

  int localWins() {
    int value = 3;
    return value;
  }

  int callGlobal() {
    return useLater(1);
  }
};

int useLater(int x) {
  Late l;
  return x + l.n;
}

class Late {
public:
  int n;
  Late() { n = 9; }
};

class Pair {
public:
  int a;
  int b;
  Pair(int x, int y) {
    a = x;
    b = y;
  }
  Pair() { }
};

class Shape {
public:
  Shape() {
    // during construction of the base part, virtual calls use the base version
    print_string(name());
  }
  virtual string name() { return "shape"; }
  string whoAmI() { return name(); }
};

class Circle : public Shape {
public:
  Circle() { print_string(name()); }
  string name() { return "circle"; }
};

int depth(int n) {
  if (n == 0) return 0;
  return 1 + depth(n - 1);
}

int main() {
  print_int(value());
  Derived d;
  print_int(d.readValue());
  print_int(d.localWins());
  print_int(d.useHelper());
  print_int(d.callGlobal());
  print_int(helper(2));

  Pair p(3, 4);
  print_int(p.a * 10 + p.b);

  Circle c;
  print_string(c.whoAmI());
  Shape& s = c;
  print_string(s.whoAmI());

  // deep recursion runs on a large interpreter stack
  print_int(depth(20000));
  return 0;
}
