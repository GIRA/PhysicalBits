visiting
visitScriptCallNode: node
	self assertScriptReference: node.
	
	(program scriptNamed: node scriptName)
		ifNotNil: [:script || callArgs scriptArgs |
			callArgs := node arguments collect: [:arg | arg key].
			scriptArgs := script arguments collect: [:arg | arg name].
			self assert: ((scriptArgs includesAllOf: callArgs)
					or: [callArgs allSatisfy: [:arg | arg = #value]])
				description: 'Explicit argument names expected'
				node: node]