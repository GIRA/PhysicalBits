accessing
serverDate
	"Returns the date string to be used in responses"

	| date |
	^String streamContents:[:s|
		date := DateAndTime now asUTC.
		s nextPutAll: date dayOfWeekAbbreviation, ', '.
		date asDate printOn: s format: #(1 2 3 $  2 1 2 ).
		s space; nextPutAll: (Time seconds: date asSeconds) print24.
		s nextPutAll: ' GMT'.
	].