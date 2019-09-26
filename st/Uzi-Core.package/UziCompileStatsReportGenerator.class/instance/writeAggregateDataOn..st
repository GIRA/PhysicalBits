as yet unclassified
writeAggregateDataOn: file
	"min, max, average, median"
	| rows min max average median size |
	rows := stats collect: [:data | data collect: [:kvp | kvp value]].
	size := rows anyOne size - 1.
	min := Array new: size .
	max := Array new: size.
	average := Array new: size.
	median := Array new: size.
	1 to: size do: [:i || data |
		data := (rows collect: [:each | each at: i + 1]) select: [:each | each isNumber].
		min at: i put: (data ifEmpty: [0] ifNotEmpty: [:d | data min]).
		max at: i put: (data ifEmpty: [0] ifNotEmpty: [:d | data max]).
		average at: i put: (data ifEmpty: [0] ifNotEmpty: [:d | data average asFloat]).
		median at: i put: (data ifEmpty: [0] ifNotEmpty: [:d | data sorted middle])].
	file nextPutAll: 'MIN,'.
	min do: [:each | file nextPutAll: each asString] separatedBy: [file nextPutAll: ','].
	file crlf.
	
	file nextPutAll: 'MAX,'.
	max do: [:each | file nextPutAll: each asString] separatedBy: [file nextPutAll: ','].
	file crlf.
	
	file nextPutAll: 'AVERAGE,'.
	average do: [:each | file nextPutAll: each asString] separatedBy: [file nextPutAll: ','].
	file crlf.
	
	file nextPutAll: 'MEDIAN,'.
	median do: [:each | file nextPutAll: each asString] separatedBy: [file nextPutAll: ','].
	file crlf.