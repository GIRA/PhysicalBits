visiting
handleScriptControl: node
	| scripts |
	scripts := node scripts collect: [:scriptName | self program scriptNamed: scriptName].
	scripts do: [:script | self visit: script].
	self visitChildren: node