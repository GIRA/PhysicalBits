accessing
allStatements
	^ program node ifNil: [^ #()]
		ifNotNil: [:root | root allNodes 
			select: [:each | each parent isBlock]]