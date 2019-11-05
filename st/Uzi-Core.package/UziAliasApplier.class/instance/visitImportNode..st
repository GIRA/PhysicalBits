visiting
visitImportNode: node
	^ (node class 
		alias: alias, node alias
		path: node path)
		token: node token;
		yourself