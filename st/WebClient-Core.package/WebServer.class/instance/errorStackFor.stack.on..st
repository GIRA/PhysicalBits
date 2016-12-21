errors
errorStackFor: anError stack: preStack on: strm
	"Provide a report for an error that's been caught during processing"

	| cnt |
	cnt := 0.
	(preStack copyFrom: 1 to: (5 min: preStack size)) do:[:ctx|
		ctx printDetails: strm.	"variable values"
		strm cr.
	].

	strm cr; nextPutAll: '--- The full stack ---'; cr.
	cnt := 0.

	preStack do:[:ctx|
		cnt := cnt + 1.
		cnt = 5 ifTrue: [
			strm nextPutAll: ' - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -'; cr].
		strm print: ctx; cr.  "just class>>selector"
	].
