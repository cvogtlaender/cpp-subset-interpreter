#include <iostream> // preprocessor lines are ignored

// Classes may be used before their definition (and so may functions).
class Account {
public:
  string owner;
  int balance;

  Account() {
    owner = "nobody";
  }

  Account(string name, int initial) {
    owner = name;
    balance = initial;
  }

  void deposit(int amount) {
    balance = balance + amount;
  }

  bool withdraw(int amount) {
    if (amount > balance) {
      return false;
    }
    balance = balance - amount;
    return true;
  }

  virtual int monthlyFee() {
    return 5;
  }

  void endOfMonth() {
    // calls the override of the dynamic type
    balance = balance - monthlyFee();
  }
};

class StudentAccount : public Account {
public:
  StudentAccount(string name) {
    owner = name;
    balance = 0;
  }

  int monthlyFee() {
    return 0;
  }
};

// Overloading by exact parameter types
void show(int value) { print_int(value); }
void show(bool value) { print_bool(value); }
void show(string label, int value) {
  print_string(label);
  print_int(value);
}
void show(Account& account) { show(account.owner, account.balance); }

// Reference parameters modify the caller's variables
void transfer(Account& from, Account& to, int amount) {
  if (from.withdraw(amount)) {
    to.deposit(amount);
  }
}

int gcd(int a, int b) {
  while (b != 0) {
    int t = b;
    b = a % b;
    a = t;
  }
  return a;
}

int main() {
  Account alice("alice", 100);
  StudentAccount bob("bob");

  transfer(alice, bob, 30);
  show(alice);
  show(bob);

  alice.endOfMonth();
  bob.endOfMonth();
  show(alice.balance);
  show(bob.balance);

  // polymorphism
  Account& any = bob;
  show(any.monthlyFee());

  // assigning a derived object to a base object slices it
  Account copy = bob;
  show(copy.monthlyFee());

  // pointers: address-of, '->' (with dynamic dispatch), heap objects
  Account* ptr = &bob;
  ptr->deposit(5);
  show(bob.balance);
  Account* carol = new StudentAccount("carol");
  show(carol->monthlyFee());
  delete carol;

  show(gcd(84, 36));
  show(gcd(17, 5) == 1);

  char grade = 'B';
  if (grade < 'C') {
    print_string("good grade");
  }
  return 0;
}
