button actions
chooseBreakpoints
	| printer src selection lines |
	^ protocol currentProgram 
		ifNil: [#()]
		ifNotNil: [:program |
			printer := UziProgramPrinter new.
			src := String streamContents: [:stream | 
				printer print: program on: stream].
			lines := Array streamContents: [:stream |
				src lineIndicesDo: [:start :ign :stop |
					stream nextPut: (start to: stop) -> (src copyFrom: start to: stop)]].
			selection := UIManager default 
				chooseMultipleFrom: (lines collect: [:each | each value])
				values: (lines collect: [:each | each key]).
			(printer intervals withIndexCollect: [:each :index | 
				(selection anySatisfy: [:sel | sel includes: each middle])
					ifTrue: [index - 1]
					ifFalse: [nil]]) 
				select: [:pc | pc notNil]]