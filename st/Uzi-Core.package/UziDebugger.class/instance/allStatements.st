accessing
allStatements
	^ program instructions select: [:each | each isStatement]