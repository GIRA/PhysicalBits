visiting
handleScriptCall: node	
	| args |
	self assert: (self program scriptNamed: node selector) notNil 
		description: 'Invalid script'
		node: node.	
	
	(self program scriptNamed: node selector)
		ifNotNil: [:script || callArgs scriptArgs |
			callArgs := node arguments collect: [:arg | arg key].
			scriptArgs := script arguments collect: [:arg | arg name].
			self assert: ((scriptArgs includesAllOf: callArgs)
					or: [callArgs allSatisfy: [:arg | arg isNil]])
				description: 'Explicit argument names expected'
				node: node].
			
	args := node arguments collect: [:each | each value].
	self visitCollection: args