private
resolveImport: import
	(librariesDirectory fileExists: import path) ifFalse: [^ nil].
	^ librariesDirectory 
		readOnlyFileNamed: import path
		do: [:file || ast |
			ast := parser parse: file contents.
			import initializationBlock ifNotNil: [:initBlock | 
				ast := self applyInitializationBlock: initBlock on: ast].
			ast := self applyOn: ast.
			ast isPetitFailure 
				ifTrue: [ast]
				ifFalse: [UziAliasApplier apply: import alias , '.' on: ast]]