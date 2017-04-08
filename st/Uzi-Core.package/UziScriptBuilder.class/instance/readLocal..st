instruction creation
readLocal: arg	
	| var |
	var := self argumentNamed: arg.
	^ self addInstruction: (UziReadLocalInstruction arg: var)