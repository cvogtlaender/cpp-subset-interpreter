// Arithmetic, comparison and logical operators on the primitive types.
bool sideEffect(bool value) {
  print_string("evaluated");
  return value;
}

int main() {
  print_int(1 + 2 * 3);
  print_int((1 + 2) * 3);
  print_int(10 - 4 - 3);
  print_int(7 / 2);
  print_int(-7 / 2);
  print_int(7 % 3);
  print_int(-7 % 3);
  print_int(-(3 - 5));
  print_int(+4);
  print_int(2147483647 + 1);
  print_int(-2147483648);

  print_bool(3 < 4);
  print_bool(4 <= 3);
  print_bool('a' < 'b');
  print_bool('z' >= 'a');
  print_bool(1 == 1);
  print_bool(true != false);
  print_bool("abc" == "abc");
  print_bool("abc" != "abd");
  print_bool('x' == 'x');

  print_bool(!true);
  print_bool(true && false || true);
  print_bool(false && sideEffect(true));
  print_bool(true || sideEffect(true));
  print_bool(true && sideEffect(false));

  print_char('A');
  print_char('\'');
  print_string("tab:\tend");
  print_string("quote: \" backslash: \\");
  print_string("");

  int x = 5;
  int y = x = 7;
  print_int(x + y);
  int a;
  bool b;
  string s;
  char c;
  print_int(a);
  print_bool(b);
  print_string(s);
  print_bool(c == '\0');
  return 0;
}
