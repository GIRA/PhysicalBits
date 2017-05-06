tests
testScriptCallAsLoopCondition2
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main [[@foo value. 1] whileTrue: [D13 toggle]]. @foo [^ 1]'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		prim arg: (UziPrimitive named: #pop).
		push arg: (program variables detect: [:var | var value = 1]).
		jz arg: 3.
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #toggle).
		jmp arg: -7
	}
