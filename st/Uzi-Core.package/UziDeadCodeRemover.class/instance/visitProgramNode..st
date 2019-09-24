visiting
visitProgramNode: node
	| roots |
	program := node.
	roots := node scripts select: [:s | s state = #running or: [s state = #once]].
	roots do: [:script | self visit: script].
	^ (UziProgramNode 
		globals: node globals
		scripts: (node scripts select: [:s | visited includes: s]))
		token: node token