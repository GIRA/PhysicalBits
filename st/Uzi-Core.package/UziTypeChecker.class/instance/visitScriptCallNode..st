visiting
visitScriptCallNode: node
	self assertScriptReference: node script.
	
	(program scriptNamed: node script name)
		ifNotNil: [:script || callArgs scriptArgs |
			callArgs := node arguments collect: [:arg | arg key].
			scriptArgs := script arguments collect: [:arg | arg name].
			self assert: ((scriptArgs includesAllOf: callArgs)
					or: [callArgs allSatisfy: [:arg | arg = #value]])
				description: 'Explicit argument names expected'
				node: node].
		
	self visit: node script