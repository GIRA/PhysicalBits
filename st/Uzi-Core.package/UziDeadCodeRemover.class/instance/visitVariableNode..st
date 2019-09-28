visiting
visitVariableNode: node
	(self localsInScope anySatisfy: [:var | var name = node name])
		ifFalse: [visitedGlobals add: node name].
	self visitChildren: node