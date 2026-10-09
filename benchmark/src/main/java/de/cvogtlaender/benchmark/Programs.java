package de.cvogtlaender.benchmark;

final class Programs {

  private Programs() {
  }

  static String fib(int n) {
    return """
        int fib(int n) {
          if (n < 2) return n;
          return fib(n - 1) + fib(n - 2);
        }
        int main() {
          return fib(%d) %% 256;
        }
        """.formatted(n);
  }

  static String loop(int iterations) {
    return """
        int main() {
          int i = 0;
          int sum = 0;
          while (i < %d) {
            if (i %% 3 == 0 || i %% 5 == 0) {
              sum = (sum + i) %% 1000007;
            }
            i = i + 1;
          }
          return sum %% 256;
        }
        """.formatted(iterations);
  }

  static String virtualDispatch(int calls) {
    return """
        class Shape {
        public:
          virtual int area() { return 0; }
        };
        class Square : public Shape {
        public:
          int side;
          Square(int s) { side = s; }
          int area() { return side * side; }
        };
        class Rect : public Shape {
        public:
          int w;
          int h;
          Rect(int a, int b) { w = a; h = b; }
          int area() { return w * h; }
        };
        int total(Shape& s, int times) {
          int sum = 0;
          while (times > 0) {
            sum = (sum + s.area()) %% 1000007;
            times = times - 1;
          }
          return sum;
        }
        int main() {
          Square sq(3);
          Rect r(2, 5);
          return (total(sq, %1$d) + total(r, %1$d)) %% 256;
        }
        """.formatted(calls / 2);
  }

  static String objectCopies(int count) {
    return """
        class Point {
        public:
          int x;
          int y;
          Point() { }
          Point(int a, int b) { x = a; y = b; }
        };
        class Point3 : public Point {
        public:
          int z;
          Point3(int a, int b, int c) { x = a; y = b; z = c; }
        };
        class Segment {
        public:
          Point from;
          Point to;
        };
        int main() {
          int i = 0;
          int sum = 0;
          Segment s;
          while (i < %d) {
            Point3 p(i, i + 1, i + 2);
            Point sliced = p;
            s.from = sliced;
            s.to = Point(i, i);
            Segment copy = s;
            sum = (sum + copy.from.y + copy.to.x) %% 1000007;
            i = i + 1;
          }
          return sum %% 256;
        }
        """.formatted(count);
  }

  static String largeProgram(int classes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < classes; i++) {
      String parent = i == 0 ? "" : " : public C" + (i - 1);
      sb.append("class C").append(i).append(parent).append(" {\npublic:\n")
          .append("  int f").append(i).append(";\n")
          .append("  C").append(i).append("() { f").append(i).append(" = ").append(i).append("; }\n")
          .append("  virtual int value() { return f").append(i).append(" * 2 + 1; }\n")
          .append("  int add").append(i).append("(int a, int b) {\n")
          .append("    if (a < b) { return a + b; } else { return a - b; }\n")
          .append("  }\n")
          .append("};\n");
      sb.append("int use").append(i).append("(C").append(i).append("& c, int n) {\n")
          .append("  int sum = 0;\n")
          .append("  while (n > 0) {\n")
          .append("    sum = sum + c.value() + c.add").append(i).append("(n, sum);\n")
          .append("    n = n - 1;\n")
          .append("  }\n")
          .append("  return sum;\n")
          .append("}\n");
    }
    sb.append("int main() {\n  C0 c;\n  return use0(c, 3);\n}\n");
    return sb.toString();
  }
}
