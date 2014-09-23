accessing
compile: aString 
	| nodes scripts |
	nodes := UziParser parse: aString.
	scripts := nodes collect: [:node | self visit: node].
	^ UziProgram scripts: scripts