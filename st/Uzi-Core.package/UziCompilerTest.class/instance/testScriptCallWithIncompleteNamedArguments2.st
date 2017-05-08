tests
testScriptCallWithIncompleteNamedArguments2
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '| a | @main [a := @foo arg2: 3]. @foo [:arg1 :arg2 | ^ arg1 * arg2]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var name isNil and: [var value = 0]]).
		push arg: (program variables detect: [:var | var value = 3]).
		call arg: #foo.
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
