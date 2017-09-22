visiting
visitScriptResumeNode: node
	node scriptNames do: [:scriptName |
		(script resume: scriptName) node: node]