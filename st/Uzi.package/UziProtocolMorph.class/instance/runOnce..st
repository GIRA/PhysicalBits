as yet unclassified
runOnce: aString
	| program |
	program := UziCompiler new 
		compile: ('#once ticking [{1}]' format: {aString}).
	protocol runOnce: program