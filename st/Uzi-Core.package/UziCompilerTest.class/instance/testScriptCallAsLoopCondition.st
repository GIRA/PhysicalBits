tests
testScriptCallAsLoopCondition
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main [[@foo value] whileTrue: [D13 toggle]]. @foo [^ 1]'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		jz arg: 3.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #toggle).
		jmp arg: -5
	}
