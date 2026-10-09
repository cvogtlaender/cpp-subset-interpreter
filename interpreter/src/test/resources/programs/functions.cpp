// Functions: recursion, overloading, reference parameters and variables,
// and use of functions before their definition.
int fib(int n) {
  if (n < 2) return n;
  return fib(n - 1) + fib(n - 2);
}

void describe(int x) { print_string("int"); }
void describe(char x) { print_string("char"); }
void describe(string x) { print_string("string"); }
void describe(bool x) { print_string("bool"); }
void describe(int x, int y) { print_string("int, int"); }

void swap(int& a, int& b) {
  int tmp = a;
  a = b;
  b = tmp;
}

void incrementCopy(int x) { x = x + 1; }
void increment(int& x) { x = x + 1; }

int isEven(int n) {
  if (n == 0) return 1;
  return isOdd(n - 1);
}

int isOdd(int n) {
  if (n == 0) return 0;
  return isEven(n - 1);
}

int main() {
  print_int(fib(20));
  describe(1);
  describe('c');
  describe("s");
  describe(true);
  describe(1, 2);

  int a = 1;
  int b = 2;
  swap(a, b);
  print_int(a);
  print_int(b);

  incrementCopy(a);
  print_int(a);
  increment(a);
  print_int(a);

  // reference variables alias their target; assignment writes through
  int& r = a;
  r = 42;
  print_int(a);
  int& rr = r;
  rr = rr + 1;
  print_int(a);
  increment(r);
  print_int(a);

  print_int(isEven(10));
  print_int(isOdd(7));
  return 0;
}
