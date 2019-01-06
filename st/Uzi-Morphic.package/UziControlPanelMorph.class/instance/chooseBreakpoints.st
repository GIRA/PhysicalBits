button actions
chooseBreakpoints
	^ protocol currentProgram 
		ifNil: [#()]
		ifNotNil: [:program || lines labels values |
			lines := program linesOfCode.
			labels := lines collect: [:each | each key copyReplaceAll: '	' with: '    '].
			self flag: #Richo. "The following code is a hack that allows the user to choose
			invalid lines and the breakpoint is placed on the next valid instruction. Except
			when the user clicks outside or at the end of a script, in which case the 
			breakpoint is invalid. It's not nice but at least it provides little surprises."
			values := (Array streamContents: [:stream || last |
				last := nil.
				lines reverseDo: [:kvp | 
					kvp value ifNotNil: [:val | last := val].
					stream nextPut: last.
					((kvp key withBlanksTrimmed beginsWith: 'task ')
						or: [(kvp key withBlanksTrimmed beginsWith: 'proc ')
						or: [kvp key withBlanksTrimmed beginsWith: 'func ']])
						ifTrue: [last := nil]]]) reversed.
			^ ((UIManager default 
				chooseMultipleFrom: labels
				values: values)
				ifNil: [#()]) 
				select: [:pc | pc notNil]]