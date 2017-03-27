visiting
visitScriptStartNode: node
	self assertScriptReference: node script.
	self visit: node script