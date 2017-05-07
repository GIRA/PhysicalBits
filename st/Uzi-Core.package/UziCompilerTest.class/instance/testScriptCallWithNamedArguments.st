tests
testScriptCallWithNamedArguments
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main [a := @foo arg1: 3 arg2: 4]. @foo [:arg1 :arg2 | ^ arg1 * arg2]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var value = 3]).
		push arg: (program variables detect: [:var | var value = 4]).
		call arg: #foo.
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
