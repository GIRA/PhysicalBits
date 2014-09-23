as yet unclassified
testEmptyScript
	| cc program |
	cc := UziCompiler new.
	program := cc compile: '#main ticking 1/s []'.
	self assert: program scripts size = 1