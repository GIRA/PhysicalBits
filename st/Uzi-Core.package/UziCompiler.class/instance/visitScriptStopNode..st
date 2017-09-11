visiting
visitScriptStopNode: node
	node scriptNames do: [:scriptName |
		(script stop: scriptName) node: node]