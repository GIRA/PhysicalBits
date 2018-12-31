accessing
nextStatements
	^ parent isBlock
		ifTrue: [parent nextStatementFrom: self]
		ifFalse: [parent nextStatements]