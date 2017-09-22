visiting
visitScriptPauseNode: node
	^ (node class scripts: (self visitCollection: node scripts)) token: node token