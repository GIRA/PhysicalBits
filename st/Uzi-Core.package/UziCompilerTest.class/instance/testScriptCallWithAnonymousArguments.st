tests
testScriptCallWithAnonymousArguments
	| program script |
	program := cc compile: '
	var a;
	task main() { a = foo(3); }
	func foo(a) { return a + 2; }'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 3]).
		call arg: #foo.
		pop arg: (program globals detect: [:var | var name = 'a']).
	}
