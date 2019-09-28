public
applyOn: anUziProgramNode
	path := OrderedCollection new.
	visitedScripts := Set new.
	visitedGlobals := Set new.
	^ self visit: anUziProgramNode