accessing
expiry: aDateAndTime
	"The expiry DateAndTime"

	expiry := aDateAndTime ifNotNil:[:dt| dt asUTC].