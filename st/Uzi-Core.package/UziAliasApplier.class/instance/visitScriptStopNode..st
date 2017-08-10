visiting
visitScriptStopNode: node
	^ (node class script: (alias, node scriptName)) token: node token