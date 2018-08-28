visiting
visitCallNode: node
	^ (node class 
		selector: (node isPrimitive 
			ifTrue: [node selector] 
			ifFalse: [alias, node selector])
		arguments: (node arguments 
			collect: [:kvp | kvp key -> (self visit: kvp value)]))
		primitiveName: node primitiveName;
		token: node token;
		yourself