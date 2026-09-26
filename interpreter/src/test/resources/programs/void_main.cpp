#include <iostream>
// preprocessor lines are skipped like comments
/* block
   comment */
void main() {
  int i = 0;
  while (true) {
    i = i + 1;
    if (i == 3) {
      print_int(i);
      return;
    }
  }
}
