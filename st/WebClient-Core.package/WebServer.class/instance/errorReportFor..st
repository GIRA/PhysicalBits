errors
errorReportFor: anError
	"Provide a report for an error that's been caught during processing"

	^[
		String streamContents:[:s| 
			self errorReportFor: anError 
				stack: ([anError signalerContext stackOfSize: 2000] 
							on: Error do:[:ex| ex return: #()])
				on: s
		].
	] on: Error do: [:ex| ex pass. 'error reporting failed'].