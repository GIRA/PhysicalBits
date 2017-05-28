tests
testScriptCallWithoutParameters
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '| a | @main [a := @foo value]. @foo [^ 42]'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		pop arg: (program globals detect: [:var | var name = 'a']).
	}
