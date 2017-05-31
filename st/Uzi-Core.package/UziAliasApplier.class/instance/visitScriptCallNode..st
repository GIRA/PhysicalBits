visiting
visitScriptCallNode: node
	^ (node class 
		script: (self visit: node script)
		arguments: (node arguments 
			collect: [:kvp | kvp key -> (self visit: kvp value)]))
		token: node token