// Classes: fields, constructors, methods, value semantics of objects.
class Counter {
public:
  int count;
  string name;

  Counter() {
    name = "anonymous";
  }

  Counter(string n) {
    name = n;
  }

  Counter(string n, int start) {
    name = n;
    count = start;
  }

  void increment() {
    count = count + 1;
  }

  void incrementBy(int n) {
    while (n > 0) {
      increment();
      n = n - 1;
    }
  }

  int get() {
    return count;
  }

  void report() {
    print_string(name);
    print_int(get());
  }
};

// no constructor: a parameterless one is synthesized
class Point {
public:
  int x;
  int y;

  int manhattan() {
    return abs(x) + abs(y);
  }
};

int abs(int v) {
  if (v < 0) return -v;
  return v;
}

class Line {
public:
  Point from;
  Point to;

  int length() {
    return abs(to.x - from.x) + abs(to.y - from.y);
  }
};

void bumpCopy(Counter c) { c.increment(); }
void bump(Counter& c) { c.increment(); }

Counter makeCounter(int start) {
  Counter c("made", start);
  return c;
}

int main() {
  Counter a;
  a.report();

  Counter b("b");
  b.increment();
  b.incrementBy(4);
  b.report();

  Counter c("c", 10);
  c.report();

  Counter direct = Counter("direct", 3);
  direct.report();

  // objects are values: assignment and by-value passing copy them
  Counter copy = b;
  copy.increment();
  print_int(b.get());
  print_int(copy.get());
  bumpCopy(b);
  print_int(b.get());
  bump(b);
  print_int(b.get());

  // references to objects alias them
  Counter& ref = b;
  ref.increment();
  print_int(b.count);

  // assignment to fields, also nested ones
  Point p;
  p.x = -3;
  p.y = 4;
  print_int(p.manhattan());

  Line l;
  l.to.x = 5;
  l.to.y = -2;
  l.from = p;
  print_int(l.length());
  p.x = 100;
  print_int(l.from.x);

  int& fieldRef = l.to.y;
  fieldRef = 7;
  print_int(l.to.y);

  print_int(makeCounter(41).get() + 1);
  makeCounter(1).report();
  return 0;
}
