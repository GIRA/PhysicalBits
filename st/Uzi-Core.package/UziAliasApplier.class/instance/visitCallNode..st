visiting
visitCallNode: node
	^ (node class 
		selector: alias, node selector
		arguments: (node arguments 
			collect: [:kvp | kvp key -> (self visit: kvp value)]))
		primitiveName: node primitiveName;
		token: node token;
		yourself