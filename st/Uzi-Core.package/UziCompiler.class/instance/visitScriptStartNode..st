visiting
visitScriptStartNode: node
	node scriptNames do: [:scriptName |
		(script start: scriptName) node: node]