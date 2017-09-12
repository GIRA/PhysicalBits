visiting
visitScriptRefNode: node
	^ (node class scriptName: alias, node scriptName) token: node token