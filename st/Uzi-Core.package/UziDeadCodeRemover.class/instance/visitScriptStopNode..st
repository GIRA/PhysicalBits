visiting
visitScriptStopNode: node	
	| scripts |
	scripts := node scripts collect: [:scriptName | program scriptNamed: scriptName].
	scripts do: [:script | self visit: script]