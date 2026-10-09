// if/else, while, blocks and the implicit conversion to bool in conditions.
int collatzSteps(int n) {
  int steps = 0;
  while (n != 1) {
    if (n % 2 == 0)
      n = n / 2;
    else
      n = 3 * n + 1;
    steps = steps + 1;
  }
  return steps;
}

string classify(int n) {
  if (n < 0) {
    return "negative";
  } else if (n == 0) {
    return "zero";
  } else if (n < 10) {
    return "small";
  }
  return "large";
}

int main() {
  print_int(collatzSteps(27));
  print_string(classify(-5));
  print_string(classify(0));
  print_string(classify(7));
  print_string(classify(100));

  // implicit bool conversion of int and char in conditions
  int countdown = 3;
  while (countdown) {
    print_int(countdown);
    countdown = countdown - 1;
  }
  char nul = '\0';
  if (nul) print_string("unreachable"); else print_string("nul is false");
  if ('a') print_string("'a' is true");

  // dangling else binds to the nearest if
  if (true)
    if (false) print_string("inner then");
    else print_string("inner else");

  // nested loops
  int i = 1;
  while (i <= 3) {
    int j = 1;
    int row = 0;
    while (j <= 3) {
      row = row * 10 + i * j;
      j = j + 1;
    }
    print_int(row);
    i = i + 1;
  }

  // shadowing in nested blocks
  int v = 1;
  {
    int v = 2;
    print_int(v);
    {
      int v = 3;
      print_int(v);
    }
    print_int(v);
  }
  print_int(v);
  return 0;
}
