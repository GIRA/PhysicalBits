public
applyOn: program
	| globals programs scripts imports primitives ast |
	programs := program imports 
		collect: [:import | self resolveImport: import].
	imports := program imports
		collect: [:import | import resolved].
	globals := Array streamContents: [:stream |
		programs do: [:each | stream nextPutAll: each globals].
		stream nextPutAll: program globals].
	scripts := Array streamContents: [:stream |
		programs do: [:each | stream nextPutAll: each scripts].
		stream nextPutAll: program scripts].
	primitives := Array streamContents: [:stream |
		programs do: [:each | stream nextPutAll: each primitives].
		stream nextPutAll: program primitives].
	ast := (UziProgramNode 
		imports: imports
		globals: globals
		scripts: scripts
		primitives: primitives)
		token: program token.
	ast := self resolvePrimitivesOn: ast.
	^ ast