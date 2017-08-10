tests
test037Program
	| program |
	program := parser parse: '
	var a = 1;
	var b = 2;
	
	task blink() {
		if read(D13) {
			turnOff(D13);
		} else {
			turnOn(D13);
		}
		delay(1000);
	}
	'.
	self assert: 2 equals: program variableDeclarations size.
	self assert: #(1 2) equals: (program variableDeclarations collect: [:v | v value value]).
	self assert: 1 equals: program scripts size.
	self assert: 2 equals: program scripts first body statements size.