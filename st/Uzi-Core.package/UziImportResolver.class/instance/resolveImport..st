private
resolveImport: import
	(librariesDirectory fileExists: import path) ifFalse: [
		self flag: #Richo. "TODO: throw an exception or something!"
		^ nil].
	^ librariesDirectory 
		readOnlyFileNamed: import path
		do: [:file || ast |
			ast := parser parse: file contents.
			import initializationBlock ifNotNil: [:initBlock | 
				ast := self applyInitializationBlock: initBlock on: ast].
			ast := self applyOn: ast.
			(ast isPetitFailure not and: [import alias notNil])
				ifTrue: [ast := self applyAlias: import alias , '.' on: ast].
			^ ast]