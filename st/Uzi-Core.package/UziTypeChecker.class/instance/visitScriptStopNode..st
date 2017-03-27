visiting
visitScriptStopNode: node
	self assertScriptReference: node script.
	self visit: node script