visiting
visitScriptNode: node
	(visited includes: node) ifTrue: [^ self].
	visited add: node.
	node children do: [:child | self visit: child]