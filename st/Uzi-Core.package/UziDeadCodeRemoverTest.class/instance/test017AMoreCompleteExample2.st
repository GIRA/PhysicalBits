as yet unclassified
test017AMoreCompleteExample2
	| actual expected original remover |
	original := Uzi parse: '
"This is just an example of code that uses all the available syntax
in the language."
"I wrote it to help me create a syntax highlighter for the ""Ace"" editor"

import foo from ''DCMotor.uzi'';
import bar from ''Sonar.uzi'' {
  trigPin = 100;
  echoPin = 200;
  stop reading;
  start reading;
  pause reading;
  resume reading;
}

var a = 10;
var b = 0.5;
var c;

task blink13() running 2/s { toggle(D13); }
task blink12() running 1/s { toggle(D12); }

task setup() {
    if a { turnOn(D11); }
    else { turnOff(D11); }
    foo_bar_baz(a, b, c);
}

func fact(n) {
    if n == 0 { return 1; }
    return n * fact(n - 1);
}

proc foo_bar_baz(a, b, c) {
    var d = a * b + c;
    repeat d { toggle(A2); }
    forever {
        start blink13, blink12;
        stop blink13;
        yield;
        pause blink12, blink13;
        resume blink12; yield;
        return;
    }
    while 1 && 0 { toggle(D10); delayMs(1000); }
    until 0 || 0 { toggle(D10); delayMs(1000); }
    while 1 >= 0; "Body is optional"
    until 0 <= 1; "Body is optional"
    do { toggle(D9); } while 1 > 0;
    do { toggle(D8); } until 0 < 1;
    for i = 0 to 10 by 1 {
        toggle(A0);
        delayMs(i * 100);
    }
	var e = foo.getSpeed();
	foo.init(fact(1 * -2 + -3.5), a + b/d, 0);
	bar.init(trig: a, echo: b, maxDist: c);
}'.
	original := Uzi resolveImports: original.
	expected := Uzi parse: '

"This is just an example of code that uses all the available syntax
in the language."
"I wrote it to help me create a syntax highlighter for the ""Ace"" editor"
"
import foo from ''DCMotor.uzi'';
import bar from ''Sonar.uzi'' {
  trigPin = 100;
  echoPin = 200;
  stop reading;
  start reading;
  pause reading;
  resume reading;
}
"
var foo.enablePin;
var foo.forwardPin;
var foo.reversePin;
var bar.trigPin = 100;
var bar.echoPin = 200;
var bar.maxDistance = 100;
var bar.distance;

var a = 10;
var b = 0.5;
var c;

prim foo.read : read;
prim bar.ping : sonarDistCm;

proc foo.init(en, f, r) {
	foo.enablePin = en;
	foo.forwardPin = f;
	foo.reversePin = r;
}

func foo.getSpeed() {
	return foo.read(foo.enablePin);
}

proc bar.init(trig, echo, maxDist) {
  bar.trigPin = trig;
  bar.echoPin = echo;
  bar.maxDistance = maxDist;
}

task bar.reading() running 10/s {
	bar.distance = bar.ping(bar.trigPin, bar.echoPin, bar.maxDistance);
}


task blink13() running 2/s { toggle(D13); }
task blink12() running 1/s { toggle(D12); }

task setup() {
    if a { turnOn(D11); }
    else { turnOff(D11); }
    foo_bar_baz(a, b, c);
}

func fact(n) {
    if n == 0 { return 1; }
    return n * fact(n - 1);
}

proc foo_bar_baz(a, b, c) {
    var d = a * b + c;
    repeat d { toggle(A2); }
    forever {
        start blink13, blink12;
        stop blink13;
        yield;
        pause blink12, blink13;
        resume blink12; yield;
        return;
    }
    while 1 && 0 { toggle(D10); delayMs(1000); }
    until 0 || 0 { toggle(D10); delayMs(1000); }
    while 1 >= 0; "Body is optional"
    until 0 <= 1; "Body is optional"
    do { toggle(D9); } while 1 > 0;
    do { toggle(D8); } until 0 < 1;
    for i = 0 to 10 by 1 {
        toggle(A0);
        delayMs(i * 100);
    }
	var e = foo.getSpeed();
	foo.init(fact(1 * -2 + -3.5), a + b/d, 0);
	bar.init(trig: a, echo: b, maxDist: c);
}'.
	expected := (Uzi resolveImports: expected) withImports: original imports.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
