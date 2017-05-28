private
parseFile: fileName withAlias: alias
	(libraries fileExists: fileName) ifFalse: [^ nil].
	^ libraries 
		readOnlyFileNamed: fileName 
		do: [:file || ast |
			ast := self parse: file contents.
			ast isPetitFailure 
				ifTrue: [ast]
				ifFalse: [ast applyAlias: alias]]