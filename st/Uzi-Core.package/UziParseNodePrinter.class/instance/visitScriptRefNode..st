visiting
visitScriptRefNode: node
	self saveIntervalFor: node 
		during: [self write: node scriptName]