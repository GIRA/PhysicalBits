tests
testScriptCallWithAnonymousArguments
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '|a| @main [a := @foo value: 3]. @foo [:a | ^ a * 2]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var value = 3]).
		call arg: #foo.
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
