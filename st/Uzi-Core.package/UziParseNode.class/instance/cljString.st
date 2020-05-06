printing
cljString
	^ String streamContents: [:s | self cljPrintOn: s]	