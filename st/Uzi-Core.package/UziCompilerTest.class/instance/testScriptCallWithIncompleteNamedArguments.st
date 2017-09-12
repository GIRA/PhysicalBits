tests
testScriptCallWithIncompleteNamedArguments
	| program script |
	program := self compile: '
	var a;
	task main() running { a = foo(arg1: 3); }
	func foo(arg1, arg2) { return arg1 * arg2; }'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program constants detect: [:var | var value = 3]).
		push arg: (program constants detect: [:var | var value = 0]).
		call arg: #foo.
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
