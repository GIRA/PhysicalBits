visiting
visitScriptNode: node
	self saveIntervalFor: node during: [
		node isTask 
			ifTrue: [^ self writeTask: node].
		node isProcedure 
			ifTrue: [^ self writeProcedure: node].
		node isFunction 
			ifTrue: [^ self writeFunction: node].
		self error: 'Invalid script type']