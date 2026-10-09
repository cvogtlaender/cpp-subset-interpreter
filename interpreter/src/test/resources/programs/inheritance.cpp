// Single inheritance, implicit base constructor calls, virtual methods,
// dynamic dispatch through references, and slicing.
class Animal {
public:
  string name;
  int legs;

  Animal() {
    name = "animal";
    legs = 4;
    print_string("Animal()");
    print_string(kind());
  }

  virtual string sound() {
    return "...";
  }

  // not virtual: always statically bound
  string kind() {
    return "generic animal";
  }

  virtual string describe() {
    return name;
  }

  void speak() {
    // unqualified virtual call dispatches on the dynamic type
    print_string(sound());
  }
};

class Dog : public Animal {
public:
  Dog() {
    name = "dog";
    print_string("Dog()");
  }

  // overrides a virtual method, so it is virtual too
  string sound() {
    return "woof";
  }

  string kind() {
    return "dog";
  }
};

class Puppy : public Dog {
public:
  int age;

  Puppy(int a) {
    age = a;
    print_string("Puppy(int)");
  }

  Puppy() {
    age = 0;
  }

  string sound() {
    return "yip";
  }

  string describe() {
    return "puppy of age";
  }
};

class Bird : public Animal {
public:
  Bird() {
    legs = 2;
  }

  virtual string sound() {
    return "tweet";
  }
};

void makeNoise(Animal& a) {
  print_string(a.sound());
}

void makeNoiseByValue(Animal a) {
  print_string(a.sound());
}

int countLegs(Animal& a, Animal& b) {
  return a.legs + b.legs;
}

int main() {
  Dog d;
  Puppy p(3);
  Bird b;

  // dynamic dispatch via references
  makeNoise(d);
  makeNoise(p);
  makeNoise(b);
  Animal& ref = p;
  print_string(ref.sound());
  print_string(ref.describe());
  print_string(ref.kind());

  // no dispatch on objects: a copy into an Animal is sliced
  makeNoiseByValue(p);
  Animal sliced = p;
  print_string(sliced.sound());
  print_string(sliced.name);

  // inherited fields and methods
  print_int(p.age);
  print_int(p.legs);
  print_string(p.name);
  print_string(p.kind());
  p.speak();
  d.speak();

  print_int(countLegs(d, b));

  // assignment of a derived object to a base variable slices
  Animal target;
  target = b;
  print_int(target.legs);
  print_string(target.sound());

  // assignment through a base reference only copies the base part
  Dog other;
  other.legs = 3;
  Animal& otherRef = other;
  otherRef = b;
  print_int(other.legs);
  print_string(other.sound());
  return 0;
}
