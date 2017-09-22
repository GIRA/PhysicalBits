visiting
visitScriptPauseNode: node
	node scriptNames do: [:scriptName |
		(script pause: scriptName) node: node]