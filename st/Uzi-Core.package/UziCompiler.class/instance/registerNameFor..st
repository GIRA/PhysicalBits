private
registerNameFor: variableDeclaration
	| name |
	name := '{1}#{2}' format: { 
		variableDeclaration name.
		variableNames size + 1 
	}.
	variableNames at: variableDeclaration put: name.
	^ name