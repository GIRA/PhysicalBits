instruction creation
writeLocal: arg
	| var |
	var := self argumentNamed: arg.
	^ self addInstruction: (UziWriteLocalInstruction arg: var)