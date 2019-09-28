visiting
visitProgramNode: node
	| roots |
	roots := node scripts select: [:s | s state = #running or: [s state = #once]].
	roots do: [:script | self visit: script].
	^ (UziProgramNode 
		globals: (node globals select: [:g | visitedGlobals includes: g name])
		scripts: (node scripts select: [:s | visitedScripts includes: s]))
		token: node token