tests
testScriptCallWithNamedArguments
	| program script |
	program := self compile: '
	var a;
	task main() { a = foo(arg1: 3, arg2: 4);}
	func foo(arg1,arg2){return arg1*arg2;}'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 3]).
		push arg: (program globals detect: [:var | var value = 4]).
		call arg: #foo.
		pop arg: (program globals detect: [:var | var name = 'a']).
	}
