tests
testScriptCallWithIncompleteNamedArguments2
	| program script |
	program := cc compile: '
	var a;
	task main() { a = foo(arg2: 3); }
	func foo(arg1, arg2) { return arg1 * arg2;}'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var name isNil and: [var value = 0]]).
		push arg: (program globals detect: [:var | var value = 3]).
		call arg: #foo.
		pop arg: (program globals detect: [:var | var name = 'a']).
	}
