visiting
visitProgramNode: node
	| roots |
	roots := node scripts select: [:s | s state = #running or: [s state = #once]].
	roots do: [:script | self visit: script].
	^ (UziProgramNode
		imports: node imports
		globals: (node globals select: [:g | visitedGlobals includes: g name])
		scripts: (node scripts select: [:s | visitedScripts includes: s])
		primitives: node primitives)
		token: node token