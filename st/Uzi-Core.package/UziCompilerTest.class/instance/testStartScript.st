tests
testStartScript
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [@main start]'.
	script := program scripts first.
	self assert: script instructions = {
		start arg: #main
	}
