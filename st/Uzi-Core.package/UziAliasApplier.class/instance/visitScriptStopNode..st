visiting
visitScriptStopNode: node
	^ (node class 
		scripts: (node scripts collect: [:scriptName | alias , scriptName])) 
		token: node token