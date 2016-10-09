installing
run: anUziProgram
	currentProgram := anUziProgram.
	globals := Dictionary new.
	^ [self send: #[0] , (UziEncoder new encode: anUziProgram)]
		ensure: ["Keep reporting the variables that remain"
			(self variables intersection: globalsReporting) 
				do: [:var | self setGlobal: var report: true]]