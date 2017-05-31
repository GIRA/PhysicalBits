visiting
visitScriptStopNode: node
	^ (node class script: (self visit: node script)) token: node token