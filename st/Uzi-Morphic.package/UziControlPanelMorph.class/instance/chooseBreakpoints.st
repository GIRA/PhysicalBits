button actions
chooseBreakpoints
	^ protocol currentProgram 
		ifNil: [#()]
		ifNotNil: [:program || lines labels values |
			lines := program linesOfCode.
			labels := lines collect: [:each | each key copyReplaceAll: '	' with: '    '].
			values := (Array streamContents: [:stream || last |
				last := nil.
				lines reverseDo: [:kvp | 
					kvp value ifNotNil: [:val | last := val].
					stream nextPut: last]]) reversed.			
			^ UIManager default 
				chooseMultipleFrom: labels
				values: values]