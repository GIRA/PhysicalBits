private
compile: aString
	| ast |
	UziCompileStatsReportGenerator current 
		register: aString 
		test: thisContext receiver selector
		class: self class.
		
	ast := Uzi parse: aString.
	ast := Uzi resolveImports: ast.
	Uzi check: ast.
	^ UziCompiler new compile: ast