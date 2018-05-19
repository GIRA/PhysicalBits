accessing
variableDeclarationsUpTo: node
	^ Array streamContents: [:stream || found |
		found := false.
		self children do: [:child |
			found := found or: [child == node].
			(found not and: [child isVariableDeclaration])
				ifTrue: [stream nextPut: child]]]