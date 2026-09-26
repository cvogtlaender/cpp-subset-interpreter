// pointers: address-of, dereference, nullptr, new/delete, '->', polymorphism
// expect-exit: 6

class Node {
public:
  int value;
  Node* next;

  Node(int v, Node* n) {
    value = v;
    next = n;
  }
};

class Shape {
public:
  virtual string name() { return "shape"; }
  string kind() { return "static shape"; }
};

class Square : public Shape {
public:
  int side;
  Square(int s) { side = s; }
  string name() { return "square"; }
  string kind() { return "static square"; }
};

class Counter {
public:
  int count;
  void inc() { count = count + 1; }
  int* countPtr() { return &count; }
};

void swap(int* a, int* b) {
  int t = *a;
  *a = *b;
  *b = t;
}

// pointer passed by reference: the callee may re-seat it
void advance(Node*& n) {
  n = n->next;
}

int length(Node* list) {
  int n = 0;
  while (list) {
    n = n + 1;
    list = list->next;
  }
  return n;
}

bool isNull(int* p) { return p == nullptr; }
bool isNull(Shape* s) { return s == nullptr; }

int main() {
  // address-of and dereference
  int x = 1;
  int y = 2;
  int* px = &x;
  *px = 10;
  print_int(x);
  swap(&x, &y);
  print_int(x);
  print_int(y);

  // pointer to pointer
  int** ppx = &px;
  **ppx = 42;
  print_int(x);
  *ppx = &y;
  print_int(*px);

  // references and pointers mix
  int& ry = *px;
  ry = 7;
  print_int(y);
  print_bool(&ry == &y);

  // nullptr, conditions and comparisons
  int* none = nullptr;
  int* unset;
  print_bool(none == unset);
  print_bool(isNull(none));
  print_bool(px != nullptr);
  if (none) {
    print_string("unreachable");
  } else {
    print_string("none is null");
  }

  // heap values
  int* heap = new int(5);
  *heap = *heap + 1;
  print_int(*heap);
  int* zero = new int;
  print_int(*zero);
  delete zero;
  delete nullptr_holder();

  // linked list on the heap
  Node* list = nullptr;
  int i = 3;
  while (i > 0) {
    list = new Node(i, list);
    i = i - 1;
  }
  print_int(length(list));
  Node* cursor = list;
  advance(cursor);
  print_int(cursor->value);
  cursor->next->value = 30;
  print_int(list->next->next->value);
  while (list != nullptr) {
    Node* rest = list->next;
    delete list;
    list = rest;
  }

  // polymorphism through base pointers
  Square sq(4);
  Shape* s = &sq;
  print_string(s->name());
  print_string(s->kind());
  print_string((*s).name());
  Shape* heapShape = new Square(2);
  print_string(heapShape->name());
  print_bool(isNull(heapShape));
  delete heapShape;

  // pointers to fields and members of objects
  Counter c;
  Counter* pc = &c;
  pc->inc();
  pc->inc();
  int* count = pc->countPtr();
  *count = *count * 10;
  print_int(c.count);
  Counter copy = *pc;
  copy.inc();
  print_int(c.count);

  return *heap;
}

int* nullptr_holder() {
  return nullptr;
}
