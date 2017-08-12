public
applyOn: program
	| globals programs scripts |
	program imports isEmpty ifTrue: [^ program].
	programs := program imports 
		collect: [:import | self resolveImport: import].
	globals := Array streamContents: [:stream |
		programs do: [:each | stream nextPutAll: each globals].
		stream nextPutAll: program globals].
	scripts := Array streamContents: [:stream |
		programs do: [:each | stream nextPutAll: each scripts].
		stream nextPutAll: program scripts].
	^ UziProgramNode 
		globals: globals
		scripts: scripts