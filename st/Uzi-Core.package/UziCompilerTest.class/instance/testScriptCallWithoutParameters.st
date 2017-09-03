tests
testScriptCallWithoutParameters
	| program script |
	program := self compile: '
	var a;
	task main() { a = foo(); }
	func foo() { return 42; }'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		pop arg: (program globals detect: [:var | var name = 'a']).
	}
