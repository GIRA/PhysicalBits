visiting
visitScriptCallNode: node
	^ (node class 
		script: alias , node scriptName
		arguments: (node arguments 
			collect: [:kvp | kvp key -> (self visit: kvp value)]))
		token: node token