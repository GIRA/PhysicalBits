tests
test055VariableDeclarationsShouldBeAllowedAnywhere
	| program script |
	program := parser parse: '
		proc foo() {
			turnOn(D13);
			var a = 1000;
			delayMs(a);
			turnOff(D13);
			var b = a;
			delayMs(b);
		}'.
	self deny: program isPetitFailure.
	script := program scriptNamed: 'foo'.
	self assert: script isProcedure.
	self assert: 6 equals: script body statements size.
	self assert: #(a b) equals: (script locals collect: [:each | each name]).
	#(2 5) do: [:i |
		self assert: (script body statements at: i) isVariableDeclaration].