visiting
visitScriptRefNode: node
	^ (node class named: alias , node name) token: node token