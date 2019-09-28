visiting
visitScriptNode: node
	(visitedScripts includes: node) ifTrue: [^ self].
	visitedScripts add: node.
	self visitChildren: node