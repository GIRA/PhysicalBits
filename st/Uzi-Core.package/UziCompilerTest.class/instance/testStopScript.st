tests
testStopScript
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [@main stop]'.
	script := program scripts first.
	self assert: script instructions = {
		stop arg: #main
	}
